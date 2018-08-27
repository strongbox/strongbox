package org.carlspring.strongbox.controllers.configuration.security.ldap;

import org.carlspring.strongbox.authentication.registry.support.AuthenticatorsScanner;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationForm;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationTestForm;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapSearchForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class LdapAuthenticatorConfigurationControllerTest
        extends RestAssuredBaseTest
{


    @Inject
    private AuthenticatorsScanner scanner;

    @Before
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
               .body("[0].rolesMapping.Developers", equalTo("REPOSITORY_MANAGER"))
               .body("[0].rolesMapping.Contributors", equalTo("USER_ROLE"))
               .body("[1].userDnPatterns[0]", equalTo("uid={0},ou=Users"))
               .body("[2].groupSearchFilter.searchBase", equalTo("ou=Groups"))
               .body("[2].groupSearchFilter.searchFilter", equalTo("(uniqueMember={0})"))
               .body("[3].userSearchFilter.searchBase", equalTo("ou=people"))
               .body("[3].userSearchFilter.searchFilter", equalTo("(uid={0})"))
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
    public void userSearchFilterShouldBeUpdatable()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               // below line is a workaround URI placeholders in RestAssuredMockMvc
               .put(getContextBaseUrl() + "/userSearchFilter/ou=guys/(uid={0})", "{0}")
               .peek()
               .then()
               .body(containsString("User search filter updated."))
               .statusCode(HttpStatus.OK.value());

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/userSearchFilter")
               .peek()
               .then()
               .body("searchFilter", equalTo("(uid={0})"))
               .body("searchBase", equalTo("ou=guys"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldUpdateFullLdapConfiguration()
    {
        LdapSearchForm groupSearchForm = new LdapSearchForm();
        groupSearchForm.setSearchBase("ou=People");
        groupSearchForm.setSearchFilter("(people={0})");

        Map<String, String> rolesMapping = new HashMap<>();
        rolesMapping.put("ArtifactsManager", "ARTIFACTS_MANAGER");
        rolesMapping.put("LogsManager", "LOGS_MANAGER");

        List<String> userDnPatterns = new ArrayList<>();
        userDnPatterns.add("uid={0},ou=AllUsers");

        LdapSearchForm userSearchForm = new LdapSearchForm();
        userSearchForm.setSearchBase("ou=Employee");
        userSearchForm.setSearchFilter("(employee={0})");

        LdapConfigurationForm form = new LdapConfigurationForm();
        form.setGroupSearch(groupSearchForm);
        form.setRolesMapping(rolesMapping);
        form.setUserDnPatterns(userDnPatterns);
        form.setUserSearch(userSearchForm);

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
               .body("[0].rolesMapping.ArtifactsManager", equalTo("ARTIFACTS_MANAGER"))
               .body("[0].rolesMapping.LogsManager", equalTo("LOGS_MANAGER"))
               .body("[1].userDnPatterns[0]", equalTo("uid={0},ou=AllUsers"))
               .body("[2].groupSearchFilter.searchBase", equalTo("ou=People"))
               .body("[2].groupSearchFilter.searchFilter", equalTo("(people={0})"))
               .body("[3].userSearchFilter.searchBase", equalTo("ou=Employee"))
               .body("[3].userSearchFilter.searchFilter", equalTo("(employee={0})"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void ldapConfigurationTestShouldFail()
    {
        LdapSearchForm groupSearchForm = new LdapSearchForm();
        groupSearchForm.setSearchBase("ou=People");
        groupSearchForm.setSearchFilter("(people={0})");

        Map<String, String> rolesMapping = new HashMap<>();
        rolesMapping.put("ArtifactsManager", "ARTIFACTS_MANAGER");
        rolesMapping.put("LogsManager", "LOGS_MANAGER");

        List<String> userDnPatterns = new ArrayList<>();
        userDnPatterns.add("uid={0},ou=AllUsers");

        LdapSearchForm userSearchForm = new LdapSearchForm();
        userSearchForm.setSearchBase("ou=Employee");
        userSearchForm.setSearchFilter("(employee={0})");

        LdapConfigurationForm subform = new LdapConfigurationForm();
        subform.setGroupSearch(groupSearchForm);
        subform.setRolesMapping(rolesMapping);
        subform.setUserDnPatterns(userDnPatterns);
        subform.setUserSearch(userSearchForm);

        LdapConfigurationTestForm form = new LdapConfigurationTestForm();
        form.setConfiguration(subform);
        form.setUsername("przemyslaw");
        form.setPassword("password");

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
    public void ldapConfigurationTestShouldPass()
    {
        LdapSearchForm groupSearchForm = new LdapSearchForm();
        groupSearchForm.setSearchBase("ou=Groups");
        groupSearchForm.setSearchFilter("(uniqueMember={0})");

        List<String> userDnPatterns = new ArrayList<>();
        userDnPatterns.add("uid={0},ou=Users");

        LdapSearchForm userSearchForm = new LdapSearchForm();
        userSearchForm.setSearchBase("ou=people");
        userSearchForm.setSearchFilter("(uid={0})");

        LdapConfigurationForm subform = new LdapConfigurationForm();
        subform.setGroupSearch(groupSearchForm);
        subform.setUserDnPatterns(userDnPatterns);
        subform.setUserSearch(userSearchForm);

        LdapConfigurationTestForm form = new LdapConfigurationTestForm();
        form.setConfiguration(subform);
        form.setUsername("przemyslaw.fusik");
        form.setPassword("password");

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
        LdapSearchForm groupSearchForm = new LdapSearchForm();
        groupSearchForm.setSearchBase("ou=Groups");
        groupSearchForm.setSearchFilter("(uniqueMember={0})");

        List<String> userDnPatterns = new ArrayList<>();
        userDnPatterns.add("uid={0},ou=Users");

        LdapSearchForm userSearchForm = new LdapSearchForm();
        userSearchForm.setSearchBase("ou=people");
        userSearchForm.setSearchFilter("(uid={0})");

        Map<String, String> rolesMapping = new HashMap<>();
        rolesMapping.put("ArtifactsManager", "ARTIFACTS_MANAGER");
        rolesMapping.put("LogsManager", "LOGS_MANAGER");

        LdapConfigurationForm subform = new LdapConfigurationForm();
        subform.setGroupSearch(groupSearchForm);
        subform.setUserDnPatterns(userDnPatterns);
        subform.setUserSearch(userSearchForm);
        subform.setRolesMapping(rolesMapping);

        LdapConfigurationTestForm form = new LdapConfigurationTestForm();
        form.setConfiguration(subform);
        form.setUsername("przemyslaw.fusik");
        form.setPassword("password");

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
    public void validationShouldWorkOnLdapFullConfuguration()
    {
        LdapSearchForm groupSearchForm = new LdapSearchForm();
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

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(ContentType.JSON)
               .body(form)
               .when()
               .put(getContextBaseUrl())
               .peek()
               .then()
               .body("message",
                     equalTo("LDAP configuration cannot be updated because the submitted form contains errors!"))
               .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void groupSearchFilterShouldBeUpdatable()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               // below line is a workaround URI placeholders in RestAssuredMockMvc
               .put(getContextBaseUrl() + "/groupSearchFilter/ou=guys/(participiant={0})", "{0}")
               .peek()
               .then()
               .body(containsString("Group search filter updated."))
               .statusCode(HttpStatus.OK.value());

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/groupSearchFilter")
               .peek()
               .then()
               .body("searchBase", equalTo("ou=guys"))
               .body("searchFilter", equalTo("(participiant={0})"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void userDnPatternsAreRemovable()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               // below line is a workaround URI placeholders in RestAssuredMockMvc
               .delete(getContextBaseUrl() + "/userDnPatterns/uid={0},ou=Users", "{0}")
               .peek()
               .then()
               .body(containsString("User DN pattern uid={0},ou=Users removed from the userDnPatterns"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void userDnPatternsAreAdditable()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               // below line is a workaround URI placeholders in RestAssuredMockMvc
               .post(getContextBaseUrl() + "/userDnPatterns/uid={0},ou=Guys", "{0}")
               .peek()
               .then()
               .body(containsString("User DN pattern uid={0},ou=Guys added to the userDnPatterns"))
               .statusCode(HttpStatus.OK.value());

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/userDnPatterns")
               .peek()
               .then()
               .body("userDnPatterns[0]", equalTo("uid={0},ou=Users"))
               .body("userDnPatterns[1]", equalTo("uid={0},ou=Guys"))
               .statusCode(HttpStatus.OK.value());
    }

}
