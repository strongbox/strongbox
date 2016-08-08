package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.rest.context.RestletTestContext;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;

/**
 * Created by Bohdan on 8/8/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@RestletTestContext
public class RoutingConfigurationManagementRestletTest
{

    @Configuration
    @ComponentScan(basePackages = { "org.carlspring.strongbox",
                                    "org.carlspring.logging" })
    public static class SpringConfig
    {

    }

    private static final RestClient restClient = new RestClient();


    @Test
    public void addAcceptedRuleSet()
            throws Exception
    {
        Response response = acceptedRuleSet();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void removeAcceptedRuleSet()
            throws Exception
    {

        Response response = acceptedRuleSet();
        response = restClient
                           .prepareTarget(
                                   "/configuration/strongbox/routing/rules/set/accepted/group-releases-2")
                           .request()
                           .delete();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void addAcceptedRepository()
            throws Exception
    {
        Response response = acceptedRuleSet();
        response = acceptedRepository();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void removeAcceptedRepository()
            throws Exception
    {
        Response response = acceptedRuleSet();
        response = acceptedRepository();
        response = restClient
                           .prepareTarget(
                                   "/configuration/strongbox/routing/rules/accepted/group-releases-2/repositories/releases3?pattern=.*some.test")
                           .request()
                           .delete();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());


    }

    @Test
    public void overrideAcceptedRepository()
            throws Exception
    {

        Response response = acceptedRuleSet();
        response = acceptedRepository();
        response = restClient
                           .prepareTarget(
                                   "/configuration/strongbox/routing/rules/accepted/group-releases-2/override/repositories")
                           .request()
                           .put(Entity.json(IOUtils.toString(
                                   getClass().getResourceAsStream("/strongbox/routing/override-repo.json"))));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    private Response acceptedRuleSet()
            throws IOException
    {
        return restClient
                       .prepareTarget("/configuration/strongbox/routing/rules/set/accepted")
                       .request()
                       .put(Entity.json(IOUtils.toString(
                               getClass().getResourceAsStream("/strongbox/routing/add-accept-rule-set.json"))));
    }

    private Response acceptedRepository()
            throws IOException
    {
        Response response;
        response = restClient
                           .prepareTarget(
                                   "/configuration/strongbox/routing/rules/accepted/group-releases-2/repositories")
                           .request()
                           .put(Entity.json(IOUtils.toString(
                                   getClass().getResourceAsStream("/strongbox/routing/add-accepted-repo.json"))));
        return response;
    }

}