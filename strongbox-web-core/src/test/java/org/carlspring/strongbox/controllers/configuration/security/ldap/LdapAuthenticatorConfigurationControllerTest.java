package org.carlspring.strongbox.controllers.configuration.security.ldap;

import org.carlspring.strongbox.authentication.registry.support.AuthenticatorsScanner;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationForm;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationTestForm;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapGroupSearchForm;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapUserSearchForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import io.restassured.http.ContentType;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;

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
    private AuthenticatorsScanner scanner;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    private static LdapConfigurationTestForm validLdapConfigurationTestForm()
    {
        LdapGroupSearchForm groupSearchForm = new LdapGroupSearchForm();
        groupSearchForm.setSearchBase("ou=Groups");
        groupSearchForm.setSearchFilter("(uniqueMember={0})");

        List<String> userDnPatterns = new ArrayList<>();
        userDnPatterns.add("uid={0},ou=Users");

        LdapUserSearchForm userSearchForm = new LdapUserSearchForm();
        userSearchForm.setSearchBase("ou=people");
        userSearchForm.setSearchFilter("(uid={0})");

        LdapConfigurationForm subform = new LdapConfigurationForm();
        subform.setRolesMapping(ImmutableMap.of("Developers", "REPOSITORY_MANAGER"));
        subform.setGroupSearch(groupSearchForm);
        subform.setUserDnPatterns(userDnPatterns);
        subform.setUserSearch(userSearchForm);
        subform.setUrl("ldap://127.0.0.1:33389/dc=carlspring,dc=com");

        LdapConfigurationTestForm form = new LdapConfigurationTestForm();
        form.setConfiguration(subform);
        form.setUsername("przemyslaw.fusik");
        form.setPassword("password");

        return form;
    }

    @BeforeEach
    public void setUp()
    {
        setContextBaseUrl("/api/configuration/ldap");
        scanner.scanAndReloadRegistry();
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
               .body("[0].url", startsWith("ldap://127.0.0.1"))
               .body("[1].managerDn", equalTo("uid=admin,ou=system"))
               .body("[2].rolesMapping.Developers", equalTo("REPOSITORY_MANAGER"))
               .body("[2].rolesMapping.Contributors", equalTo("USER_ROLE"))
               .body("[3].userDnPatterns[0]", equalTo("uid={0},ou=Users"))
               .body("[4].groupSearchFilter.searchBase", equalTo("ou=Groups"))
               .body("[4].groupSearchFilter.searchFilter", equalTo("(uniqueMember={0})"))
               .body("[5].userSearchFilter.searchBase", equalTo("ou=people"))
               .body("[5].userSearchFilter.searchFilter", equalTo("(uid={0})"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldReturnProperRolesMapping()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/rolesMapping")
               .peek()
               .then()
               .body("rolesMapping.Developers", equalTo("REPOSITORY_MANAGER"))
               .body("rolesMapping.Contributors", equalTo("USER_ROLE"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void userDnPatternsContainExpectedUserDnPattern()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/userDnPatterns")
               .peek()
               .then()
               .body("userDnPatterns[0]", equalTo("uid={0},ou=Users"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void userSearchFilterEqualsExpectedValue()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/userSearchFilter")
               .peek()
               .then()
               .body("searchFilter", equalTo("(uid={0})"))
               .body("searchBase", equalTo("ou=people"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void groupSearchFilterEqualsExpectedValue()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/groupSearchFilter")
               .peek()
               .then()
               .body("searchBase", equalTo("ou=Groups"))
               .body("searchFilter", equalTo("(uniqueMember={0})"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldUpdateExistingMapping()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .put(getContextBaseUrl() + "/rolesMapping/Developers/REPOSITORY_READER")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldAddNewMapping()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .put(getContextBaseUrl() + "/rolesMapping/Managers/REPOSITORY_MANAGER")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldNotAllowToAddNewMappingOnExistingKey()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .post(getContextBaseUrl() + "/rolesMapping/Contributors/REPOSITORY_READER")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldAllowToAddNewMappingOnNotExistingKey()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .post(getContextBaseUrl() + "/rolesMapping/Testers/REPOSITORY_READER")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldAllowToDeleteExistingMapping()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(getContextBaseUrl() + "/rolesMapping/Contributors")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldNotAllowToDeleteNotExistingMapping()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(getContextBaseUrl() + "/rolesMapping/Testers")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldBeAbleToDropConfiguration()
            throws IOException
    {
        Resource backup = new ClassPathResource("etc/conf/strongbox-external-user-providers.xml");
        File temp = File.createTempFile("backup-strongbox-external-user-providers.xml", ".tmp");
        FileUtils.copyFile(backup.getFile(), temp);

        shouldReturnProperLdapConfiguration();

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .when()
               .delete(getContextBaseUrl())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek()
               .then()
               .body("message", equalTo("LDAP is not configured"))
               .statusCode(HttpStatus.BAD_REQUEST.value());

        FileUtils.copyFile(temp, backup.getFile());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldBeAbleToPutNewConfiguration()
            throws IOException
    {
        shouldReturnProperLdapConfiguration();

        shouldBeAbleToDropConfiguration();

        shouldUpdateFullLdapConfiguration();
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldUpdateFullLdapConfiguration()
            throws IOException
    {
        Resource backup = new ClassPathResource("etc/conf/strongbox-external-user-providers.xml");
        File temp = File.createTempFile("backup-strongbox-external-user-providers.xml", ".tmp");
        FileUtils.copyFile(backup.getFile(), temp);

        LdapGroupSearchForm groupSearchForm = new LdapGroupSearchForm();
        groupSearchForm.setSearchBase("ou=People");
        groupSearchForm.setSearchFilter("(people={0})");

        Map<String, String> rolesMapping = new HashMap<>();
        rolesMapping.put("ArtifactsManager", "ARTIFACTS_MANAGER");
        rolesMapping.put("LogsManager", "LOGS_MANAGER");

        List<String> userDnPatterns = new ArrayList<>();
        userDnPatterns.add("uid={0},ou=AllUsers");

        LdapUserSearchForm userSearchForm = new LdapUserSearchForm();
        userSearchForm.setSearchBase("ou=Employee");
        userSearchForm.setSearchFilter("(employee={0})");

        LdapConfigurationForm form = new LdapConfigurationForm();
        form.setGroupSearch(groupSearchForm);
        form.setRolesMapping(rolesMapping);
        form.setUserDnPatterns(userDnPatterns);
        form.setUserSearch(userSearchForm);
        form.setUrl("ldap://127.0.0.1:33389/dc=carlspring,dc=com");

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
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
               .body("[0].url", equalTo("ldap://127.0.0.1:33389/"))
               .body("[1].managerDn", equalTo(""))
               .body("[2].rolesMapping.ArtifactsManager", equalTo("ARTIFACTS_MANAGER"))
               .body("[2].rolesMapping.LogsManager", equalTo("LOGS_MANAGER"))
               .body("[3].userDnPatterns[0]", equalTo("uid={0},ou=AllUsers"))
               .body("[4].groupSearchFilter.searchBase", equalTo("ou=People"))
               .body("[4].groupSearchFilter.searchFilter", equalTo("(people={0})"))
               .body("[5].userSearchFilter.searchBase", equalTo("ou=Employee"))
               .body("[5].userSearchFilter.searchFilter", equalTo("(employee={0})"))
               .statusCode(HttpStatus.OK.value());

        FileUtils.copyFile(temp, backup.getFile());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldUpdateFullLdapConfigurationWithManagerDn()
            throws IOException
    {
        Resource backup = new ClassPathResource("etc/conf/strongbox-external-user-providers.xml");
        File temp = File.createTempFile("backup-strongbox-external-user-providers.xml", ".tmp");
        FileUtils.copyFile(backup.getFile(), temp);

        LdapGroupSearchForm groupSearchForm = new LdapGroupSearchForm();
        groupSearchForm.setSearchBase("ou=People");
        groupSearchForm.setSearchFilter("(people={0})");

        Map<String, String> rolesMapping = new HashMap<>();
        rolesMapping.put("ArtifactsManager", "ARTIFACTS_MANAGER");
        rolesMapping.put("LogsManager", "LOGS_MANAGER");

        List<String> userDnPatterns = new ArrayList<>();
        userDnPatterns.add("uid={0},ou=AllUsers");

        LdapUserSearchForm userSearchForm = new LdapUserSearchForm();
        userSearchForm.setSearchBase("ou=Employee");
        userSearchForm.setSearchFilter("(employee={0})");

        LdapConfigurationForm form = new LdapConfigurationForm();
        form.setGroupSearch(groupSearchForm);
        form.setRolesMapping(rolesMapping);
        form.setUserDnPatterns(userDnPatterns);
        form.setUserSearch(userSearchForm);
        form.setManagerDn("uid=admin,ou=system");
        form.setManagerPassword("secret");
        form.setUrl("ldap://127.0.0.1:33389/dc=carlspring,dc=com");

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
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
               .body("[0].url", equalTo("ldap://127.0.0.1:33389/"))
               .body("[1].managerDn", equalTo("uid=admin,ou=system"))
               .body("[2].rolesMapping.ArtifactsManager", equalTo("ARTIFACTS_MANAGER"))
               .body("[2].rolesMapping.LogsManager", equalTo("LOGS_MANAGER"))
               .body("[3].userDnPatterns[0]", equalTo("uid={0},ou=AllUsers"))
               .body("[4].groupSearchFilter.searchBase", equalTo("ou=People"))
               .body("[4].groupSearchFilter.searchFilter", equalTo("(people={0})"))
               .body("[5].userSearchFilter.searchBase", equalTo("ou=Employee"))
               .body("[5].userSearchFilter.searchFilter", equalTo("(employee={0})"))
               .statusCode(HttpStatus.OK.value());

        FileUtils.copyFile(temp, backup.getFile());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestRequiresUrl()
    {
        LdapConfigurationTestForm form = validLdapConfigurationTestForm();
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
        LdapConfigurationTestForm form = validLdapConfigurationTestForm();
        form.getConfiguration().setUrl("dc=carlspring,dc=com");

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
        LdapConfigurationTestForm form = validLdapConfigurationTestForm();

        List<String> userDnPatterns = new ArrayList<>();
        userDnPatterns.add("uid={0},ou=AllUsers");

        LdapUserSearchForm userSearchForm = new LdapUserSearchForm();
        userSearchForm.setSearchBase("ou=Employee");
        userSearchForm.setSearchFilter("(employee={0})");

        LdapConfigurationForm subform = form.getConfiguration();
        subform.setUserDnPatterns(userDnPatterns);
        subform.setUserSearch(userSearchForm);

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
        LdapConfigurationTestForm form = validLdapConfigurationTestForm();
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
        LdapConfigurationTestForm form = validLdapConfigurationTestForm();

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
        LdapConfigurationTestForm form = validLdapConfigurationTestForm();
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
        LdapConfigurationTestForm form = validLdapConfigurationTestForm();
        Map<String, String> rolesMapping = new HashMap<>();
        rolesMapping.put("ArtifactsManager", "ARTIFACTS_MANAGER");
        rolesMapping.put("LogsManager", "LOGS_MANAGER");
        form.getConfiguration().setRolesMapping(rolesMapping);

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

}
