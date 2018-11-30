package org.carlspring.strongbox.controllers.configuration.security.ldap;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.carlspring.strongbox.authentication.ConfigurableProviderManager;
import org.carlspring.strongbox.authentication.external.ldap.LdapAuthenticationConfigurationManager;
import org.carlspring.strongbox.authentication.external.ldap.LdapConfiguration;
import org.carlspring.strongbox.authentication.external.ldap.LdapRoleMapping;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationForm;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationTestForm;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapGroupSearchForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.restassured.http.ContentType;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class LdapAuthenticatorConfigurationControllerTest
        extends RestAssuredBaseTest
{
    @Inject
    private ConfigurableProviderManager providerManager;

    @Inject
    private LdapAuthenticationConfigurationManager ldapAuthenticationConfigurationManager;

    @Override
    @BeforeEach
    public void init()
        throws Exception
    {
        super.init();

        setContextBaseUrl("/api/configuration/ldap");
        providerManager.reload();
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldReturnProperLdapConfiguration()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek()
               .then()
               .body("url", startsWith("ldap://127.0.0.1"))
               .body("groupSearchBase", equalTo("ou=Groups"))
               .body("groupSearchFilter", equalTo("(uniqueMember={0})"))
               .body("searchSubtree", equalTo(true))
               .body("groupRoleAttribute", equalTo("cn"))
               .body("rolePrefix", equalTo(""))
               .body("convertToUpperCase", equalTo(false))
               .body("roleMappingList[0].ldapRole", equalTo("Developers"))
               .body("roleMappingList[0].strongboxRole", equalTo("REPOSITORY_MANAGER"))
               .body("roleMappingList[1].ldapRole", equalTo("Contributors"))
               .body("roleMappingList[1].strongboxRole", equalTo("USER_ROLE"))
               .body("userDnPatternList[0]", equalTo("uid={0},ou=Users"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldUpdateFullLdapConfiguration()
        throws IOException
    {

        LdapConfiguration configuration = ldapAuthenticationConfigurationManager.getConfiguration();

        configuration.setGroupSearchBase("ou=People");
        configuration.setGroupSearchFilter("(people={0})");

        configuration.getRoleMappingList().add(new LdapRoleMapping("ArtifactsManager", "ARTIFACTS_MANAGER"));
        configuration.getRoleMappingList().add(new LdapRoleMapping("LogsManager", "LOGS_MANAGER"));

        configuration.getUserDnPatternList().add("uid={0},ou=AllUsers");
        configuration.setUrl("ldap://127.0.0.1:33389/dc=carlspring,dc=com");

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(configuration)
               .when()
               .put(getContextBaseUrl())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek()
               .then()
               .body("url", startsWith("ldap://127.0.0.1"))
               .body("groupSearchBase", equalTo("ou=People"))
               .body("groupSearchFilter", equalTo("(people={0})"))
               .body("searchSubtree", equalTo(true))
               .body("groupRoleAttribute", equalTo("cn"))
               .body("rolePrefix", equalTo(""))
               .body("convertToUpperCase", equalTo(false))
               .body("roleMappingList[0].ldapRole", equalTo("Developers"))
               .body("roleMappingList[0].strongboxRole", equalTo("REPOSITORY_MANAGER"))
               .body("roleMappingList[1].ldapRole", equalTo("Contributors"))
               .body("roleMappingList[1].strongboxRole", equalTo("USER_ROLE"))
               .body("roleMappingList[2].ldapRole", equalTo("ArtifactsManager"))
               .body("roleMappingList[2].strongboxRole", equalTo("ARTIFACTS_MANAGER"))
               .body("roleMappingList[3].ldapRole", equalTo("LogsManager"))
               .body("roleMappingList[3].strongboxRole", equalTo("LOGS_MANAGER"))
               .body("userDnPatternList[0]", equalTo("uid={0},ou=Users"))
               .body("userDnPatternList[1]", equalTo("uid={0},ou=AllUsers"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestRequiresUrl()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.getConfiguration().setUrl(null);

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("errors[0]['name']", equalTo("configuration.url"))
               .body("errors[0]['messages'][0]", equalTo("must not be empty"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestRequiresValidUrl()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.getConfiguration().setUrl("http://host:port?thisIsWrongUrl=true");

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("errors[0]['name']", equalTo("configuration.url"))
               .body("errors[0]['messages'][0]", equalTo("must be a valid URI"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldFail()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();

        List<String> userDnPatterns = new ArrayList<>();
        userDnPatterns.add("uid={0},ou=AllUsers");

        LdapConfiguration subform = form.getConfiguration();
        subform.setUserDnPatternList(userDnPatterns);
        subform.setGroupSearchBase("ou=Employee");
        subform.setGroupSearchFilter("(employee={0})");

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test failed"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldFailWithInvalidUserDn()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.getConfiguration().setManagerDn("daddy");
        form.getConfiguration().setManagerPassword("mummy");

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test failed"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldPassWithoutUserDn()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test passed"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldPassWithUserDn()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.getConfiguration().setManagerDn("uid=admin,ou=system");
        form.getConfiguration().setManagerPassword("secret");

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test passed"));
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldNotAffectInternalConfiguration()
    {
        LdapConfigurationTestForm form = getLdapConfigurationTestForm();
        form.getConfiguration()
            .setRoleMappingList(Stream.of(new LdapRoleMapping("ArtifactsManager", "ARTIFACTS_MANAGER"),
                                          new LdapRoleMapping("LogsManager", "LOGS_MANAGER"))
                                      .collect(Collectors.toList()));

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl() + "/test")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("LDAP configuration test passed"));

        shouldReturnProperLdapConfiguration();
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void validationShouldWorkOnLdapFullConfiguration()
    {
        LdapGroupSearchForm groupSearchForm = new LdapGroupSearchForm();
        groupSearchForm.setSearchBase("ou=People");

        Map<String, String> rolesMapping = new HashMap<>();
        rolesMapping.put("ArtifactsManager", "ARTIFACTS_MANAGER");
        rolesMapping.put("LogsManager", "LOGS_MANAGER");

        List<String> userDnPatterns = new ArrayList<>();
        userDnPatterns.add("uid={0},ou=AllUsers");

        LdapConfigurationForm form = new LdapConfigurationForm();
        form.setGroupSearch(groupSearchForm);
        form.setRolesMapping(rolesMapping);
        form.setUserDnPatterns(userDnPatterns);
        form.setUrl("ldap://127.0.0.1:33389/dc=carlspring,dc=com");

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl())
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private LdapConfigurationTestForm getLdapConfigurationTestForm()
    {
        LdapConfigurationTestForm form = new LdapConfigurationTestForm();

        LdapConfiguration configuration = ldapAuthenticationConfigurationManager.getConfiguration();
        form.setConfiguration(configuration);
        return form;
    }
}
