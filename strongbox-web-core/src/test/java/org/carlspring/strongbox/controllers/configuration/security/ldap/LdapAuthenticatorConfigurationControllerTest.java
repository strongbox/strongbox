package org.carlspring.strongbox.controllers.configuration.security.ldap;

import org.carlspring.strongbox.authentication.registry.support.AuthenticatorsScanner;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class LdapAuthenticatorConfigurationControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    private AuthenticatorsScanner scanner;


    @Before
    public void setUp()
    {
        scanner.scanAndReloadRegistry();
    }

    @Test
    public void shouldReturnProperRolesMapping()
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/api/configuration/ldap/rolesMapping")
                          .peek()
                          .then()
                          .body("roles-mapping.Developers",
                                CoreMatchers.equalTo("REPOSITORY_MANAGER"))
                          .body("roles-mapping.Contributors",
                                CoreMatchers.equalTo("USER_ROLE"))
                          .statusCode(200);
    }

    @Test
    public void userDnPatternsContainExpectedUserDnPattern()
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/api/configuration/ldap/userDnPatterns")
                          .peek()
                          .then()
                          .body("user-dn-pattern[0]",
                                CoreMatchers.equalTo("uid={0},ou=Users"))
                          .statusCode(200);
    }

    @Test
    public void userSearchFilterEqualsExpectedValue()
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/api/configuration/ldap/userSearchFilter")
                          .peek()
                          .then()
                          .body("search-filter",
                                CoreMatchers.equalTo("(uid={0})"))
                          .body("search-base",
                                CoreMatchers.equalTo("ou=people"))
                          .statusCode(200);
    }

    @Test
    public void groupSearchFilterEqualsExpectedValue()
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/api/configuration/ldap/groupSearchFilter")
                          .peek()
                          .then()
                          .body("search-base",
                                CoreMatchers.equalTo("ou=Groups"))
                          .body("search-filter",
                                CoreMatchers.equalTo("(uniqueMember={0})"))
                          .statusCode(200);
    }


    @Test
    public void shouldUpdateExistingMapping()
    {
        RestAssuredMockMvc.when()
                          .put("/api/configuration/ldap/rolesMapping/Developers/REPOSITORY_READER")
                          .peek()
                          .then()
                          .statusCode(200);
    }

    @Test
    public void shouldAddNewMapping()
    {
        RestAssuredMockMvc.when()
                          .put("/api/configuration/ldap/rolesMapping/Managers/REPOSITORY_MANAGER")
                          .peek()
                          .then()
                          .statusCode(200);
    }

    @Test
    public void shouldNotAllowToAddNewMappingOnExistingKey()
    {
        RestAssuredMockMvc.when()
                          .post("/api/configuration/ldap/rolesMapping/Contributors/REPOSITORY_READER")
                          .peek()
                          .then()
                          .statusCode(400);
    }

    @Test
    public void shouldAllowToAddNewMappingOnNotExistingKey()
    {
        RestAssuredMockMvc.when()
                          .post("/api/configuration/ldap/rolesMapping/Testers/REPOSITORY_READER")
                          .peek()
                          .then()
                          .statusCode(200);
    }

    @Test
    public void shouldAllowToDeleteExistingMapping()
    {
        RestAssuredMockMvc.when()
                          .delete("/api/configuration/ldap/rolesMapping/Contributors")
                          .peek()
                          .then()
                          .statusCode(200);
    }

    @Test
    public void shouldNotAllowToDeleteNotExistingMapping()
    {
        RestAssuredMockMvc.when()
                          .delete("/api/configuration/ldap/rolesMapping/Testers")
                          .peek()
                          .then()
                          .statusCode(400);
    }

    @Test
    public void userSearchFilterShouldBeUpdatable()
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/xml")
                          .when()
                          // below line is a workaround URI placeholders in RestAssuredMockMvc
                          .put("/api/configuration/ldap/userSearchFilter/ou=guys/(uid={0})", "{0}")
                          .peek()
                          .then()
                          .body(CoreMatchers.equalTo("User search filter updated."))
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/api/configuration/ldap/userSearchFilter")
                          .peek()
                          .then()
                          .body("search-filter",
                                CoreMatchers.equalTo("(uid={0})"))
                          .body("search-base",
                                CoreMatchers.equalTo("ou=guys"))
                          .statusCode(200);
    }

    @Test
    public void groupSearchFilterShouldBeUpdatable()
    {
        RestAssuredMockMvc.given()
                          // below line is a workaround URI placeholders in RestAssuredMockMvc
                          .put("/api/configuration/ldap/groupSearchFilter/ou=guys/(participiant={0})", "{0}")
                          .peek()
                          .then()
                          .body(CoreMatchers.equalTo("Group search filter updated."))
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/api/configuration/ldap/groupSearchFilter")
                          .peek()
                          .then()
                          .body("search-base",
                                CoreMatchers.equalTo("ou=guys"))
                          .body("search-filter",
                                CoreMatchers.equalTo("(participiant={0})"))
                          .statusCode(200);
    }

    @Test
    public void userDnPatternsAreRemovable()
    {
        RestAssuredMockMvc.given()
                          // below line is a workaround URI placeholders in RestAssuredMockMvc
                          .delete("/api/configuration/ldap/userDnPatterns/uid={0},ou=Users", "{0}")
                          .peek()
                          .then()
                          .body(CoreMatchers.equalTo(
                                  "user DN pattern uid={0},ou=Users removed from the userDnPatterns"))
                          .statusCode(200);
    }

    @Test
    public void userDnPatternsAreAdditable()
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/xml")
                          .when()
                          // below line is a workaround URI placeholders in RestAssuredMockMvc
                          .post("/api/configuration/ldap/userDnPatterns/uid={0},ou=Guys", "{0}")
                          .peek()
                          .then()
                          .body(CoreMatchers.equalTo(
                                  "user DN pattern uid={0},ou=Guys added to the userDnPatterns"))
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/api/configuration/ldap/userDnPatterns")
                          .peek()
                          .then()
                          .body("user-dn-pattern[0]",
                                CoreMatchers.equalTo("uid={0},ou=Users"))
                          .body("user-dn-pattern[1]",
                                CoreMatchers.equalTo("uid={0},ou=Guys"))
                          .statusCode(200);
    }

}
