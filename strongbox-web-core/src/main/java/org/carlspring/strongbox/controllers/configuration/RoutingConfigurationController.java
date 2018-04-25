package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.forms.storage.routing.RoutingRuleForm;
import org.carlspring.strongbox.forms.storage.routing.RuleSetForm;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;
import org.carlspring.strongbox.validation.RequestBodyValidationException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Pablo Tirado
 */
@Controller
@RequestMapping("/api/configuration/strongbox/routing")
@Api(value = "/api/configuration/strongbox/routing")
public class RoutingConfigurationController
        extends BaseConfigurationController
{

    private static final String SUCCESSFUL_ADD_RULE_SET = "Rule set addition succeeded.";
    private static final String FAILED_ADD_RULE_SET_FORM_ERROR = "Rule set cannot be added because the submitted form contains errors!";
    private static final String NOT_FOUND_RULE_SET = "Rule set could not be found.";

    private static final String SUCCESSFUL_REMOVE_RULE_SET = "Rule set deletion succeeded.";

    private static final String SUCCESSFUL_ADD_REPOSITORY = "Accepted repository addition succeeded.";
    private static final String FAILED_ADD_REPOSITORY_FORM_ERROR = "Accepted repository cannot be added because the submitted form contains errors!";
    private static final String NOT_FOUND_REPOSITORY = "Accepted repository could not be found.";

    private static final String SUCCESSFUL_REMOVE_REPOSITORY = "Accepted repository deletion succeeded.";

    private static final String SUCCESSFUL_OVERRIDE_REPOSITORY = "Accepted repository override succeeded.";
    private static final String FAILED_OVERRIDE_REPOSITORY_FORM_ERROR = "Accepted repository cannot be overridden because the submitted form contains errors!";

    private static final String NOT_FOUND_ROUTING_RULE = "Routing rule is empty.";

    private final ConversionService conversionService;

    public RoutingConfigurationController(ConfigurationManagementService configurationManagementService,
                                          ConversionService conversionService)
    {
        super(configurationManagementService);
        this.conversionService = conversionService;
    }

    @ApiOperation(value = "Returns routing rules.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Everything went ok.") })
    @GetMapping(value = "/rules",
                produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getRoutingRules()
    {
        RoutingRules body = configurationManagementService.getRoutingRules();
        return ResponseEntity.ok(body);
    }

    @ApiOperation(value = "Adds an accepted rule set.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_ADD_RULE_SET),
                            @ApiResponse(code = 400, message = FAILED_ADD_RULE_SET_FORM_ERROR),
                            @ApiResponse(code = 404, message = NOT_FOUND_RULE_SET) })
    @PutMapping(value = "/rules/set/accepted",
                consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addAcceptedRuleSet(@RequestBody @Validated RuleSetForm ruleSetForm,
                                             BindingResult bindingResult,
                                             @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_ADD_RULE_SET_FORM_ERROR, bindingResult);
        }

        RuleSet ruleSet = conversionService.convert(ruleSetForm, RuleSet.class);
        final boolean added = configurationManagementService.saveAcceptedRuleSet(ruleSet);
        return getResponse(added, SUCCESSFUL_ADD_RULE_SET, NOT_FOUND_RULE_SET, acceptHeader);
    }

    @ApiOperation(value = "Removes and accepted rule set.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_REMOVE_RULE_SET),
                            @ApiResponse(code = 404, message = NOT_FOUND_RULE_SET) })
    @DeleteMapping(value = "/rules/set/accepted/{groupRepository}",
                   consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity removeAcceptedRuleSet(@PathVariable String groupRepository,
                                                @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        final boolean removed = configurationManagementService.removeAcceptedRuleSet(groupRepository);
        return getResponse(removed, SUCCESSFUL_REMOVE_RULE_SET, NOT_FOUND_RULE_SET, acceptHeader);
    }

    @ApiOperation(value = "Adds an accepted repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_ADD_REPOSITORY),
                            @ApiResponse(code = 400, message = FAILED_ADD_REPOSITORY_FORM_ERROR),
                            @ApiResponse(code = 404, message = NOT_FOUND_REPOSITORY) })
    @PutMapping(value = "/rules/accepted/{groupRepository}/repositories",
                consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addAcceptedRepository(@PathVariable String groupRepository,
                                                @RequestBody @Validated RoutingRuleForm routingRuleForm,
                                                BindingResult bindingResult,
                                                @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_ADD_REPOSITORY_FORM_ERROR, bindingResult);
        }

        RoutingRule routingRule = conversionService.convert(routingRuleForm, RoutingRule.class);
        if (routingRule != null && routingRule.getPattern() == null && routingRule.getRepositories()
                                                                                  .isEmpty())
        {
            return getNotFoundResponseEntity(NOT_FOUND_ROUTING_RULE, acceptHeader);
        }

        final boolean saved = configurationManagementService.saveAcceptedRepository(groupRepository, routingRule);
        return getResponse(saved, SUCCESSFUL_ADD_REPOSITORY, NOT_FOUND_REPOSITORY, acceptHeader);
    }

    @ApiOperation(value = "Removes an accepted repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_REMOVE_REPOSITORY),
                            @ApiResponse(code = 404, message = NOT_FOUND_REPOSITORY) })
    @DeleteMapping(value = "/rules/accepted/{groupRepository}/repositories/{repositoryId}",
                   consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity removeAcceptedRepository(@PathVariable String groupRepository,
                                                   @PathVariable String repositoryId,
                                                   @RequestParam("pattern") String pattern,
                                                   @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        final boolean removed = configurationManagementService.removeAcceptedRepository(groupRepository, pattern,
                                                                                        repositoryId);
        return getResponse(removed, SUCCESSFUL_REMOVE_REPOSITORY, NOT_FOUND_REPOSITORY, acceptHeader);
    }

    @ApiOperation(value = "Overrides an accepted repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_OVERRIDE_REPOSITORY),
                            @ApiResponse(code = 400, message = FAILED_OVERRIDE_REPOSITORY_FORM_ERROR),
                            @ApiResponse(code = 404, message = NOT_FOUND_REPOSITORY) })
    @PutMapping(value = "/rules/accepted/{groupRepository}/override/repositories",
                consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity overrideAcceptedRepository(@PathVariable String groupRepository,
                                                     @RequestBody @Validated RoutingRuleForm routingRuleForm,
                                                     BindingResult bindingResult,
                                                     @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_OVERRIDE_REPOSITORY_FORM_ERROR, bindingResult);
        }

        RoutingRule routingRule = conversionService.convert(routingRuleForm, RoutingRule.class);
        if (routingRule != null && routingRule.getPattern() == null && routingRule.getRepositories().isEmpty())
        {
            return getNotFoundResponseEntity(NOT_FOUND_ROUTING_RULE, acceptHeader);
        }

        final boolean overridden = configurationManagementService.overrideAcceptedRepositories(groupRepository,
                                                                                               routingRule);
        return getResponse(overridden, SUCCESSFUL_OVERRIDE_REPOSITORY, NOT_FOUND_REPOSITORY, acceptHeader);
    }

    private ResponseEntity getResponse(boolean result,
                                       String successfulMessage,
                                       String notFoundMessage,
                                       String acceptHeader)
    {
        if (result)
        {
            return getSuccessfulResponseEntity(successfulMessage, acceptHeader);
        }
        else
        {
            return getNotFoundResponseEntity(notFoundMessage, acceptHeader);
        }
    }

}
