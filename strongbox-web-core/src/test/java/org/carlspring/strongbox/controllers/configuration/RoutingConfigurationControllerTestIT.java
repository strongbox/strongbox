package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RuleSet;

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
    public void addAcceptedRuleSet()
            throws Exception
    {
        acceptedRuleSet();
    }

    @Test
    public void removeAcceptedRuleSet()
            throws Exception
    {
        acceptedRuleSet();

        String url = getContextBaseUrl() + "/rules/set/accepted/group-releases-2";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(url)
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void addAcceptedRepository()
            throws Exception
    {
        acceptedRuleSet();
        acceptedRepository();
    }

    @Test
    public void removeAcceptedRepository()
            throws Exception
    {
        acceptedRuleSet();
        acceptedRepository();

        String url = getContextBaseUrl() + "/rules/accepted" +
                     "/group-releases-2/repositories/releases3?pattern=.*some.test";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(url)
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void overrideAcceptedRepository()
            throws Exception
    {
        acceptedRuleSet();
        acceptedRepository();

        String url = getContextBaseUrl() + "/rules/accepted" +
                     "/group-releases-2/override/repositories";

        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(".*some.test");
        Set<String> repositories = new HashSet<>();
        repositories.add("releases22");
        repositories.add("releases32");
        routingRule.setRepositories(repositories);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(routingRule)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    private void acceptedRuleSet()
    {
        String url = getContextBaseUrl() + "/rules/set/accepted";

        RuleSet ruleSet = new RuleSet();
        ruleSet.setGroupRepository("group-releases-2");
        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(".*some.test");
        Set<String> repositories = new HashSet<>();
        repositories.add("releases-with-trash");
        repositories.add("releases-with-redeployment");
        routingRule.setRepositories(repositories);

        List<RoutingRule> rule = new LinkedList<>();
        rule.add(routingRule);
        ruleSet.setRoutingRules(rule);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(ruleSet)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    private void acceptedRepository()
    {
        String url =
                getContextBaseUrl() + "/rules/accepted/group-releases-2/repositories";

        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(".*some.test");
        Set<String> repositories = new HashSet<>();
        repositories.add("releases2");
        repositories.add("releases3");
        routingRule.setRepositories(repositories);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(routingRule)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value());
    }
}
