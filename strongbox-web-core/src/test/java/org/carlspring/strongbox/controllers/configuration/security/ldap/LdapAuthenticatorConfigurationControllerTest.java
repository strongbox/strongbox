package org.carlspring.strongbox.controllers.configuration.security.ldap;

import org.carlspring.strongbox.authentication.registry.support.AuthenticatorsScanner;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;

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
            throws Exception
    {
        setContextBaseUrl("/api/configuration/ldap");
        scanner.scanAndReloadRegistry();
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldReturnProperRolesMapping()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/rolesMapping")
               .peek()
               .then()
               .body("roles-mapping.Developers", equalTo("REPOSITORY_MANAGER"))
               .body("roles-mapping.Contributors", equalTo("USER_ROLE"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void userDnPatternsContainExpectedUserDnPattern()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/userDnPatterns")
               .peek()
               .then()
               .body("user-dn-pattern[0]", equalTo("uid={0},ou=Users"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void userSearchFilterEqualsExpectedValue()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/userSearchFilter")
               .peek()
               .then()
               .body("search-filter", equalTo("(uid={0})"))
               .body("search-base", equalTo("ou=people"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void groupSearchFilterEqualsExpectedValue()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/groupSearchFilter")
               .peek()
               .then()
               .body("search-base", equalTo("ou=Groups"))
               .body("search-filter", equalTo("(uniqueMember={0})"))
               .statusCode(HttpStatus.OK.value());
    }


    @WithMockUser(authorities = "ADMIN")
    @Test
    public void shouldUpdateExistingMapping()
            throws Exception
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
            throws Exception
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
            throws Exception
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
            throws Exception
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
            throws Exception
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
            throws Exception
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
            throws Exception
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
               .body("search-filter", equalTo("(uid={0})"))
               .body("search-base", equalTo("ou=guys"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void groupSearchFilterShouldBeUpdatable()
            throws Exception
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
               .body("search-base", equalTo("ou=guys"))
               .body("search-filter", equalTo("(participiant={0})"))
               .statusCode(HttpStatus.OK.value());
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void userDnPatternsAreRemovable()
            throws Exception
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
            throws Exception
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
               .body("user-dn-pattern[0]", equalTo("uid={0},ou=Users"))
               .body("user-dn-pattern[1]", equalTo("uid={0},ou=Guys"))
               .statusCode(HttpStatus.OK.value());
    }

}
