package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.storage.routing.RoutingRuleForm;
import org.carlspring.strongbox.forms.storage.routing.RoutingRuleRepositoryForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;
import org.carlspring.strongbox.storage.routing.MutableRoutingRules;
import org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum;

import java.util.UUID;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.controllers.configuration.RoutingConfigurationController.*;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class RoutingConfigurationControllerTestIT
        extends RestAssuredBaseTest
{

    public static final String GROUP_RELEASES_2 = "group-releases-2";

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl("/api/configuration/strongbox/routing/rules");
    }

    @AfterEach
    public void cleanup()
    {
        MutableRoutingRules routingRules = getRoutingRules();
        routingRules.getRules().stream().filter(r -> r.getRepositoryId().contains(GROUP_RELEASES_2))
                    .forEach(r -> removeRoutingRule(MediaType.APPLICATION_JSON_VALUE, r.getUuid()));
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testAddAndRemoveRoutingRule(String acceptHeader)
    {
        addRoutingRule(acceptHeader);
        MutableRoutingRules routingRules = getRoutingRules();
        assertThat(routingRules).isNotNull();
        assertThat(routingRules.getRules().size()).isEqualTo(2);
        MutableRoutingRule lastRule = routingRules.getRules().get(routingRules.getRules().size() - 1);

        removeRoutingRule(acceptHeader, lastRule.getUuid());

        routingRules = getRoutingRules();
        assertThat(routingRules).isNotNull();
        assertThat(routingRules.getRules().size()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testShouldNotAddAcceptedRuleSet(String acceptHeader)
    {
        shouldNotAddRoutingRule(acceptHeader);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testAddAndUpdateAndRemoveRoutingRule(String acceptHeader)
    {
        addRoutingRule(acceptHeader);

        MutableRoutingRules routingRules = getRoutingRules();
        assertThat(routingRules).isNotNull();
        assertThat(routingRules.getRules().size()).isEqualTo(2);
        MutableRoutingRule lastRule = routingRules.getRules().get(routingRules.getRules().size() - 1);

        updateRoutingRule(acceptHeader, lastRule.getUuid());

        routingRules = getRoutingRules();

        assertThat(routingRules).isNotNull();
        assertThat(routingRules.getRules().size()).isEqualTo(2);
        lastRule = routingRules.getRules().get(routingRules.getRules().size() - 1);
        assertThat(lastRule.getRepositoryId()).isEqualTo("group-releases-2-updated");
        assertThat(lastRule.getPattern()).isEqualTo(".*some.test-updated");
        assertThat(lastRule.getType()).isEqualTo(RoutingRuleTypeEnum.DENY.getType());

        removeRoutingRule(acceptHeader, lastRule.getUuid());

        routingRules = getRoutingRules();
        assertThat(routingRules).isNotNull();
        assertThat(routingRules.getRules().size()).isEqualTo(1);
    }

    private void addRoutingRule(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/add";

        RoutingRuleForm routingRuleForm = new RoutingRuleForm();
        routingRuleForm.setPattern(".*some.test");
        routingRuleForm.setType(RoutingRuleTypeEnum.ACCEPT);
        routingRuleForm.setRepositoryId(GROUP_RELEASES_2);
        RoutingRuleRepositoryForm routingRuleRepositoryForm = new RoutingRuleRepositoryForm();
        routingRuleRepositoryForm.setRepositoryId("releases-with-trash");
        RoutingRuleRepositoryForm routingRuleRepositoryForm2 = new RoutingRuleRepositoryForm();
        routingRuleRepositoryForm2.setRepositoryId("releases-with-redeployment");
        routingRuleForm.setRepositories(Lists.newArrayList(routingRuleRepositoryForm, routingRuleRepositoryForm2));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(routingRuleForm)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_ADD_ROUTING_RULE));
    }

    private void updateRoutingRule(String acceptHeader,
                                   UUID uuid)
    {
        String url = getContextBaseUrl() + "/update/" + uuid.toString();

        RoutingRuleForm routingRuleForm = new RoutingRuleForm();
        routingRuleForm.setPattern(".*some.test-updated");
        routingRuleForm.setType(RoutingRuleTypeEnum.DENY);
        routingRuleForm.setRepositoryId(GROUP_RELEASES_2 + "-updated");
        RoutingRuleRepositoryForm routingRuleRepositoryForm = new RoutingRuleRepositoryForm();
        routingRuleRepositoryForm.setRepositoryId("releases-with-trash");
        RoutingRuleRepositoryForm routingRuleRepositoryForm2 = new RoutingRuleRepositoryForm();
        routingRuleRepositoryForm2.setRepositoryId("releases-with-redeployment");
        routingRuleForm.setRepositories(Lists.newArrayList(routingRuleRepositoryForm, routingRuleRepositoryForm2));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(routingRuleForm)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(FAILED_UPDATE_ROUTING_RULE));
    }

    private void shouldNotAddRoutingRule(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/add";

        RoutingRuleForm routingRuleForm = new RoutingRuleForm();
        routingRuleForm.setPattern("");
        routingRuleForm.setType(RoutingRuleTypeEnum.ACCEPT);
        routingRuleForm.setRepositories(Lists.newArrayList());

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(routingRuleForm)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_ADD_ROUTING_RULE_FORM_ERRORS));
    }

    private void removeRoutingRule(String acceptHeader,
                                   UUID uuid)
    {
        String url = getContextBaseUrl() + "/remove/" + uuid.toString();

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .when()
               .delete(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_REMOVE_ROUTING_RULE));
    }

    private MutableRoutingRules getRoutingRules()
    {
        String url = getContextBaseUrl();

        return given().contentType(MediaType.APPLICATION_JSON_VALUE)
                      .accept(MediaType.APPLICATION_JSON_VALUE)
                      .when()
                      .get(url)
                      .then()
                      .statusCode(HttpStatus.OK.value())
                      .extract()
                      .as(MutableRoutingRules.class);
    }

}
