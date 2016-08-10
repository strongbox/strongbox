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

    public static final String ADD_ACCEPTED_RULE_SET_JSON = "{\n" +
                                                            "  \"rule-set\": {\n" +
                                                            "    \"group-repository\": \"group-releases-2\",\n" +
                                                            "    \"rule\": [\n" +
                                                            "      {\n" +
                                                            "        \"pattern\": \".*some.test\",\n" +
                                                            "        \"repository\": [\n" +
                                                            "          \"releases-with-trash\",\n" +
                                                            "          \"releases-with-redeployment\"\n" +
                                                            "        ]\n" +
                                                            "      }\n" +
                                                            "    ]\n" +
                                                            "  }\n" +
                                                            "}";
    public static final String ADD_ACCEPTED_REPO_JSON = "{\n" +
                                                        "  \"rule\": {\n" +
                                                        "    \"pattern\": \".*some.test\",\n" +
                                                        "    \"repository\": [\n" +
                                                        "      \"releases2\",\n" +
                                                        "      \"releases3\"\n" +
                                                        "    ]\n" +
                                                        "  }\n" +
                                                        "}";
    public static final String OVERRIDE_REPO_JSON = "{\n" +
                                                    "          \"rule\":\n" +
                                                    "            {\n" +
                                                    "              \"pattern\": \".*some.test\",\n" +
                                                    "              \"repository\": [\n" +
                                                    "                \"releases22\", \"releases32\"\n" +
                                                    "              ]\n" +
                                                    "            }\n" +
                                                    "\n" +
                                                    "}";

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
                           .put(Entity.json(OVERRIDE_REPO_JSON));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    private Response acceptedRuleSet()
            throws IOException
    {
        return restClient
                       .prepareTarget("/configuration/strongbox/routing/rules/set/accepted")
                       .request()
                       .put(Entity.json(ADD_ACCEPTED_RULE_SET_JSON));
    }

    private Response acceptedRepository()
            throws IOException
    {
        Response response;
        response = restClient
                           .prepareTarget(
                                   "/configuration/strongbox/routing/rules/accepted/group-releases-2/repositories")
                           .request()
                           .put(Entity.json(ADD_ACCEPTED_REPO_JSON));
        return response;
    }

}
