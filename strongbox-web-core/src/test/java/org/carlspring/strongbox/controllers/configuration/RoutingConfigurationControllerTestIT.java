package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.storage.routing.RoutingRuleForm;
import org.carlspring.strongbox.forms.storage.routing.RuleSetForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.controllers.configuration.RoutingConfigurationController.*;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class RoutingConfigurationControllerTestIT
        extends RestAssuredBaseTest
{

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl("/api/configuration/strongbox/routing");
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testAddAcceptedRuleSet(String acceptHeader)
            throws Exception
    {
        acceptedRuleSet(acceptHeader);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testShouldNotAddAcceptedRuleSet(String acceptHeader)
            throws Exception
    {
        shouldNotAddAcceptedRuleSet(acceptHeader);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testRemoveAcceptedRuleSet(String acceptHeader)
            throws Exception
    {
        acceptedRuleSet(acceptHeader);
        removeAcceptedRuleSet(acceptHeader);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testAddAcceptedRepository(String acceptHeader)
            throws Exception
    {
        acceptedRuleSet(acceptHeader);
        acceptedRepository(acceptHeader);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testShouldNotAddAcceptedRepository(String acceptHeader)
            throws Exception
    {
        acceptedRuleSet(acceptHeader);
        shouldNotAddAcceptedRepository(acceptHeader);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testRemoveAcceptedRepository(String acceptHeader)
            throws Exception
    {
        acceptedRuleSet(acceptHeader);
        acceptedRepository(acceptHeader);
        removeAcceptedRepository(acceptHeader);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testOverrideAcceptedRepository(String acceptHeader)
            throws Exception
    {
        acceptedRuleSet(acceptHeader);
        acceptedRepository(acceptHeader);
        overrideAcceptedRepository(acceptHeader);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testShouldNotOverrideAcceptedRepository(String acceptHeader)
            throws Exception
    {
        acceptedRuleSet(acceptHeader);
        acceptedRepository(acceptHeader);
        shouldNotOverrideAcceptedRepository(acceptHeader);
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
