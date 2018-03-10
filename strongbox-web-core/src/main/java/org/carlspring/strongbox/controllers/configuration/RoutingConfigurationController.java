package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RuleSet;

import io.swagger.annotations.Api;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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

    public RoutingConfigurationController(ConfigurationManagementService configurationManagementService)
    {
        super(configurationManagementService);
    }

    @RequestMapping(value = "/rules",
                    method = RequestMethod.GET,
                    produces = { MediaType.APPLICATION_JSON_VALUE,
                                 MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity getRoutingRules()
    {
        return ResponseEntity.ok(configurationManagementService.getRoutingRules());
    }

    @RequestMapping(value = "/rules/set/accepted",
                    method = RequestMethod.PUT,
                    consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addAcceptedRuleSet(@RequestBody RuleSet ruleSet)
    {
        final boolean added = configurationManagementService.saveAcceptedRuleSet(ruleSet);
        if (added)
        {
            return ResponseEntity.ok().build();
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @RequestMapping(value = "/rules/set/accepted/{groupRepository}",
                    method = RequestMethod.DELETE,
                    consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity removeAcceptedRuleSet(@PathVariable String groupRepository)
    {
        return getResponse(configurationManagementService.removeAcceptedRuleSet(groupRepository));
    }

    @RequestMapping(value = "/rules/accepted/{groupRepository}/repositories",
                    method = RequestMethod.PUT,
                    consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addAcceptedRepository(@PathVariable String groupRepository,
                                                @RequestBody RoutingRule routingRule)
    {
        logger.debug("[addAcceptedRepository] Routing rule " + routingRule);

        if (routingRule.getPattern() == null && routingRule.getRepositories()
                                                           .isEmpty())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Routing rule is empty");
        }

        return getResponse(configurationManagementService.saveAcceptedRepository(groupRepository, routingRule));
    }

    @RequestMapping(value = "/rules/accepted/{groupRepository}/repositories/{repositoryId}",
                    method = RequestMethod.DELETE,
                    consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity removeAcceptedRepository(@PathVariable String groupRepository,
                                                   @PathVariable String repositoryId,
                                                   @RequestParam("pattern") String pattern)
    {
        return getResponse(
                configurationManagementService.removeAcceptedRepository(groupRepository, pattern, repositoryId));
    }

    @RequestMapping(value = "/rules/accepted/{groupRepository}/override/repositories",
                    method = RequestMethod.PUT,
                    consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity overrideAcceptedRepository(@PathVariable String groupRepository,
                                                     @RequestBody RoutingRule routingRule)
    {
        logger.debug("[addAcceptedRepository] Routing rule " + routingRule);

        if (routingRule.getPattern() == null && routingRule.getRepositories().isEmpty())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Routing rule is empty");
        }

        return getResponse(configurationManagementService.overrideAcceptedRepositories(groupRepository, routingRule));
    }

    private ResponseEntity getResponse(boolean result)
    {
        if (result)
        {
            return ResponseEntity.ok().build();
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
