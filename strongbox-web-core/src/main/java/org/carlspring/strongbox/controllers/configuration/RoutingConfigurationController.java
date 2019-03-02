package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.forms.storage.routing.RoutingRuleForm;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;
import org.carlspring.strongbox.storage.routing.MutableRoutingRules;
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
 * @author Przemyslaw Fusik
 */
@Controller
@RequestMapping("/api/configuration/strongbox/routing/rules")
@Api(value = "/api/configuration/strongbox/routing/rules")
public class RoutingConfigurationController
        extends BaseConfigurationController
{

    static final String SUCCESSFUL_ADD_ROUTING_RULE = "Successfully added routing rule.";

    static final String FAILED_ADD_ROUTING_RULE_FORM_ERRORS = "Routing rule cannot be added because the submitted form contains errors!";

    static final String FAILED_ADD_ROUTING_RULE = "Routing rule cannot be added.";

    static final String SUCCESSFUL_REMOVE_ROUTING_RULE = "Routing rule removed successfully.";

    static final String FAILED_REMOVE_ROUTING_RULE = "Routing rule cannot be removed.";

    static final String SUCCESSFUL_ADD_REPOSITORY = "Accepted repository added successfully.";

    static final String FAILED_ADD_REPOSITORY_FORM_ERROR = "Accepted repository cannot be added because the submitted form contains errors!";

    static final String NOT_FOUND_REPOSITORY = "Accepted repository could not be found.";

    static final String SUCCESSFUL_REMOVE_REPOSITORY = "Accepted repository deleted successfully.";

    static final String SUCCESSFUL_UPDATE_ROUTING_RULE = "Accepted repository override succeeded.";

    static final String FAILED_OVERRIDE_REPOSITORY_FORM_ERROR = "Accepted repository cannot be overridden because the submitted form contains errors!";


    private final ConversionService conversionService;


    public RoutingConfigurationController(ConfigurationManagementService configurationManagementService,
                                          ConversionService conversionService)
    {
        super(configurationManagementService);
        this.conversionService = conversionService;
    }

    @ApiOperation(value = "Returns routing rules.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Everything went ok.") })
    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getRoutingRules()
    {
        MutableRoutingRules body = configurationManagementService.getMutableConfigurationClone()
                                                                 .getRoutingRules();
        return ResponseEntity.ok(body);
    }

    @ApiOperation(value = "Adds a routing rule.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_ADD_ROUTING_RULE),
                            @ApiResponse(code = 400, message = FAILED_ADD_ROUTING_RULE_FORM_ERRORS),
                            @ApiResponse(code = 404, message = FAILED_ADD_ROUTING_RULE) })
    @PutMapping(value = "/add",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity add(@RequestBody @Validated RoutingRuleForm routingRule,
                              BindingResult bindingResult,
                              @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_ADD_ROUTING_RULE_FORM_ERRORS, bindingResult);
        }

        MutableRoutingRule rule = conversionService.convert(routingRule, MutableRoutingRule.class);
        final boolean added = configurationManagementService.addRoutingRule(rule);

        return getResponse(added, SUCCESSFUL_ADD_ROUTING_RULE, FAILED_ADD_ROUTING_RULE, acceptHeader);
    }

    @ApiOperation(value = "Removes routing rule at the specified index.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_REMOVE_ROUTING_RULE),
                            @ApiResponse(code = 404, message = FAILED_ADD_ROUTING_RULE) })
    @DeleteMapping(value = "/remove/{index}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity remove(@PathVariable int index,
                                 @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        final boolean removed = configurationManagementService.removeRoutingRule(index);

        return getResponse(removed, SUCCESSFUL_REMOVE_ROUTING_RULE, FAILED_REMOVE_ROUTING_RULE, acceptHeader);
    }

    @ApiOperation(value = "Updates routing rule at the specified index.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_UPDATE_ROUTING_RULE),
                            @ApiResponse(code = 400, message = FAILED_OVERRIDE_REPOSITORY_FORM_ERROR),
                            @ApiResponse(code = 404, message = NOT_FOUND_REPOSITORY) })
    @PutMapping(value = "/update/{index}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity update(@PathVariable int index,
                                 @RequestBody @Validated RoutingRuleForm routingRule,
                                 BindingResult bindingResult,
                                 @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_OVERRIDE_REPOSITORY_FORM_ERROR, bindingResult);
        }

        MutableRoutingRule rule = conversionService.convert(routingRule, MutableRoutingRule.class);

        final boolean updated = configurationManagementService.updateRoutingRule(index, rule);
        return getResponse(updated, SUCCESSFUL_UPDATE_ROUTING_RULE, NOT_FOUND_REPOSITORY, acceptHeader);
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
