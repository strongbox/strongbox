package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.services.RoutingRulesService;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RuleSet;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Bohdan on 8/6/2016.
 */
@Component
@Path("/configuration/strongbox/routing/rules/")
@Api(value = "/configuration/strongbox/routing/rules/")
public class RoutingConfigurationManagementRestlet
        extends BaseRestlet
{

    @Autowired
    private RoutingRulesService routingRulesService;

    @GET
    @Produces({ MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_XML })
    public Response getRoutingRules()
    {
        return Response.ok(routingRulesService.getRoutingRules()).build();
    }

    @PUT
    @Path("/set/accepted")
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response addAcceptedRuleSet(RuleSet ruleSet)
    {
        final boolean added = routingRulesService.addAcceptedRuleSet(ruleSet);
        if (added)
        {
            return Response.ok().build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/set/accepted/{groupRepository}")
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response removeAcceptedRuleSet(@PathParam("groupRepository") String groupRepository)
    {
        return getResponse(routingRulesService.removeAcceptedRuleSet(groupRepository));
    }

    @PUT
    @Path("/accepted/{groupRepository}/repositories")
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response addAcceptedRepository(@PathParam("groupRepository") String groupRepository,
                                          RoutingRule routingRule)
    {
        return getResponse(routingRulesService.addAcceptedRepository(groupRepository, routingRule));
    }

    @DELETE
    @Path("/accepted/{groupRepository}/repositories/{repositoryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response removeAcceptedRepository(@PathParam("groupRepository") String groupRepository,
                                             @PathParam("repositoryId") String repositoryId,
                                             @QueryParam("pattern") String pattern)
    {
        return getResponse(routingRulesService.removeAcceptedRepository(groupRepository, pattern, repositoryId));
    }

    @PUT
    @Path("/accepted/{groupRepository}/override/repositories")
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response overrideAcceptedRepository(@PathParam("groupRepository") String groupRepository,
                                               RoutingRule routingRule)
    {
        return getResponse(routingRulesService.overrideAcceptedRepositories(groupRepository, routingRule));
    }

    private Response getResponse(boolean result)
    {
        if (result)
        {
            return Response.ok().build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

}
