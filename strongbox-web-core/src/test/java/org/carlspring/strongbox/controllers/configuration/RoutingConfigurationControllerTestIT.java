package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.storage.routing.RoutingRuleForm;
import org.carlspring.strongbox.forms.storage.routing.RuleSetForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.controllers.configuration.RoutingConfigurationController.*;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class RoutingConfigurationControllerTestIT
        extends RestAssuredBaseTest
{

    @Before
    public void setUp()
    {
        setContextBaseUrl("/api/configuration/strongbox/routing");
    }

    @Test
    public void addAcceptedRuleSetWithTextAcceptHeader()
            throws Exception
    {
        acceptedRuleSet(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void addAcceptedRuleSetWithJsonAcceptHeader()
            throws Exception
    {
        acceptedRuleSet(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void shouldNotAddAcceptedRuleSetWithTextAcceptHeader()
            throws Exception
    {
        shouldNotAddAcceptedRuleSet(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void shouldNotAddAcceptedRuleSetWithJsonAcceptHeader()
            throws Exception
    {
        shouldNotAddAcceptedRuleSet(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void removeAcceptedRuleSetWithTextAcceptHeader()
            throws Exception
    {
        acceptedRuleSet(MediaType.TEXT_PLAIN_VALUE);
        removeAcceptedRuleSet(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void removeAcceptedRuleSetWithJsonAcceptHeader()
            throws Exception
    {
        acceptedRuleSet(MediaType.APPLICATION_JSON_VALUE);
        removeAcceptedRuleSet(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void addAcceptedRepositoryWithTextAcceptHeader()
            throws Exception
    {
        acceptedRuleSet(MediaType.TEXT_PLAIN_VALUE);
        acceptedRepository(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void addAcceptedRepositoryWithJsonAcceptHeader()
            throws Exception
    {
        acceptedRuleSet(MediaType.APPLICATION_JSON_VALUE);
        acceptedRepository(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void shouldNotAddAcceptedRepositoryWithTextAcceptHeader()
            throws Exception
    {
        acceptedRuleSet(MediaType.TEXT_PLAIN_VALUE);
        shouldNotAddAcceptedRepository(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void shouldNotAddAcceptedRepositoryWithJsonAcceptHeader()
            throws Exception
    {
        acceptedRuleSet(MediaType.APPLICATION_JSON_VALUE);
        shouldNotAddAcceptedRepository(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void removeAcceptedRepositoryWithTextAcceptHeader()
            throws Exception
    {
        acceptedRuleSet(MediaType.TEXT_PLAIN_VALUE);
        acceptedRepository(MediaType.TEXT_PLAIN_VALUE);
        removeAcceptedRepository(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void removeAcceptedRepositoryWithJsonAcceptHeader()
            throws Exception
    {
        acceptedRuleSet(MediaType.APPLICATION_JSON_VALUE);
        acceptedRepository(MediaType.APPLICATION_JSON_VALUE);
        removeAcceptedRepository(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void overrideAcceptedRepositoryWithTextAcceptHeader()
            throws Exception
    {
        acceptedRuleSet(MediaType.TEXT_PLAIN_VALUE);
        acceptedRepository(MediaType.TEXT_PLAIN_VALUE);
        overrideAcceptedRepository(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void overrideAcceptedRepositoryWithJsonAcceptHeader()
            throws Exception
    {
        acceptedRuleSet(MediaType.APPLICATION_JSON_VALUE);
        acceptedRepository(MediaType.APPLICATION_JSON_VALUE);
        overrideAcceptedRepository(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void shouldNotOverrideAcceptedRepositoryWithTextAcceptHeader()
            throws Exception
    {
        acceptedRuleSet(MediaType.TEXT_PLAIN_VALUE);
        acceptedRepository(MediaType.TEXT_PLAIN_VALUE);
        shouldNotOverrideAcceptedRepository(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void shouldNotOverrideAcceptedRepositoryWithJsonAcceptHeader()
            throws Exception
    {
        acceptedRuleSet(MediaType.APPLICATION_JSON_VALUE);
        acceptedRepository(MediaType.APPLICATION_JSON_VALUE);
        shouldNotOverrideAcceptedRepository(MediaType.APPLICATION_JSON_VALUE);
    }

    private void acceptedRuleSet(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/rules/set/accepted";

        RuleSetForm ruleSet = new RuleSetForm();
        ruleSet.setGroupRepository("group-releases-2");
        RoutingRuleForm routingRule = new RoutingRuleForm();
        routingRule.setPattern(".*some.test");
        Set<String> repositories = new HashSet<>();
        repositories.add("releases-with-trash");
        repositories.add("releases-with-redeployment");
        routingRule.setRepositories(repositories);

        List<RoutingRuleForm> rule = new LinkedList<>();
        rule.add(routingRule);
        ruleSet.setRoutingRules(rule);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(ruleSet)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_ADD_RULE_SET));
    }

    private void shouldNotAddAcceptedRuleSet(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/rules/set/accepted";

        RuleSetForm ruleSet = new RuleSetForm();
        ruleSet.setGroupRepository("");
        RoutingRuleForm routingRule = new RoutingRuleForm();
        routingRule.setPattern("");
        Set<String> repositories = new HashSet<>();
        routingRule.setRepositories(repositories);

        List<RoutingRuleForm> rule = new LinkedList<>();
        rule.add(routingRule);
        ruleSet.setRoutingRules(rule);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(ruleSet)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_ADD_RULE_SET_FORM_ERROR));
    }

    private void removeAcceptedRuleSet(String acceptHeader)
    {

        String url = getContextBaseUrl() + "/rules/set/accepted/group-releases-2";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .when()
               .delete(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_REMOVE_RULE_SET));
    }

    private void acceptedRepository(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/rules/accepted/group-releases-2/repositories";

        RoutingRuleForm routingRule = new RoutingRuleForm();
        routingRule.setPattern(".*some.test");
        Set<String> repositories = new HashSet<>();
        repositories.add("releases2");
        repositories.add("releases3");
        routingRule.setRepositories(repositories);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(routingRule)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_ADD_REPOSITORY));
    }

    private void shouldNotAddAcceptedRepository(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/rules/accepted/group-releases-2/repositories";

        RoutingRuleForm routingRule = new RoutingRuleForm();
        routingRule.setPattern("");
        Set<String> repositories = new HashSet<>();
        routingRule.setRepositories(repositories);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(routingRule)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_ADD_REPOSITORY_FORM_ERROR));
    }

    private void removeAcceptedRepository(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/rules/accepted" +
                     "/group-releases-2/repositories/releases3?pattern=.*some.test";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .when()
               .delete(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_REMOVE_REPOSITORY));
    }

    private void overrideAcceptedRepository(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/rules/accepted" +
                     "/group-releases-2/override/repositories";

        RoutingRuleForm routingRule = new RoutingRuleForm();
        routingRule.setPattern(".*some.test");
        Set<String> repositories = new HashSet<>();
        repositories.add("releases22");
        repositories.add("releases32");
        routingRule.setRepositories(repositories);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(routingRule)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_OVERRIDE_REPOSITORY));
    }

    private void shouldNotOverrideAcceptedRepository(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/rules/accepted" +
                     "/group-releases-2/override/repositories";

        RoutingRuleForm routingRule = new RoutingRuleForm();
        routingRule.setPattern("");
        Set<String> repositories = new HashSet<>();
        routingRule.setRepositories(repositories);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(routingRule)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_OVERRIDE_REPOSITORY_FORM_ERROR));
    }
}
