package org.carlspring.strongbox.controllers.security.ldap;

import org.carlspring.strongbox.authentication.registry.support.AuthenticatorsScanner;
import org.carlspring.strongbox.controllers.context.IntegrationTest;
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
            throws Exception
    {
        scanner.scanAndReloadRegistry();
    }

    @Test
    public void shouldReturnProperRolesMapping()
            throws Exception
    {
        RestAssuredMockMvc.when()
                          .get("/configuration/ldap/rolesMapping")
                          .peek()
                          .then()
                          .body("authoritiesExternalToInternalMapper.rolesMapping.entry[0].key",
                                CoreMatchers.equalTo("Developers"))
                          .body("authoritiesExternalToInternalMapper.rolesMapping.entry[0].value",
                                CoreMatchers.equalTo("REPOSITORY_MANAGER"))
                          .body("authoritiesExternalToInternalMapper.rolesMapping.entry[1].key",
                                CoreMatchers.equalTo("Contributors"))
                          .body("authoritiesExternalToInternalMapper.rolesMapping.entry[1].value",
                                CoreMatchers.equalTo("USER_ROLE"))
                          .statusCode(200);
    }

    @Test
    public void userDnPatternsContainExpectedUserDnPattern()
            throws Exception
    {
        RestAssuredMockMvc.when()
                          .get("/configuration/ldap/userDnPatterns")
                          .peek()
                          .then()
                          .body("userDnPatterns.userDnPattern[0]",
                                CoreMatchers.equalTo("uid={0},ou=Users"))
                          .statusCode(200);
    }

    @Test
    public void userSearchFilterEqualsExpectedValue()
            throws Exception
    {
        RestAssuredMockMvc.when()
                          .get("/configuration/ldap/userSearchFilter")
                          .peek()
                          .then()
                          .body("userSearch.searchFilter",
                                CoreMatchers.equalTo("(uid={0})"))
                          .body("userSearch.searchBase",
                                CoreMatchers.equalTo("ou=people"))
                          .statusCode(200);
    }

    @Test
    public void groupSearchFilterEqualsExpectedValue()
            throws Exception
    {
        RestAssuredMockMvc.when()
                          .get("/configuration/ldap/groupSearchFilter")
                          .peek()
                          .then()
                          .body("groupSearch.searchBase",
                                CoreMatchers.equalTo("ou=Groups"))
                          .body("groupSearch.searchFilter",
                                CoreMatchers.equalTo("(uniqueMember={0})"))
                          .statusCode(200);
    }


    @Test
    public void shouldUpdateExistingMapping()
            throws Exception
    {
        RestAssuredMockMvc.when()
                          .put("/configuration/ldap/rolesMapping/Developers/REPOSITORY_READER")
                          .peek()
                          .then()
                          .statusCode(200);
    }

    @Test
    public void shouldAddNewMapping()
            throws Exception
    {
        RestAssuredMockMvc.when()
                          .put("/configuration/ldap/rolesMapping/Managers/REPOSITORY_MANAGER")
                          .peek()
                          .then()
                          .statusCode(200);
    }

    @Test
    public void shouldNotAllowToAddNewMappingOnExistingKey()
            throws Exception
    {
        RestAssuredMockMvc.when()
                          .post("/configuration/ldap/rolesMapping/Contributors/REPOSITORY_READER")
                          .peek()
                          .then()
                          .statusCode(400);
    }

    @Test
    public void shouldAllowToAddNewMappingOnNotExistingKey()
            throws Exception
    {
        RestAssuredMockMvc.when()
                          .post("/configuration/ldap/rolesMapping/Testers/REPOSITORY_READER")
                          .peek()
                          .then()
                          .statusCode(200);
    }

    @Test
    public void shouldAllowToDeleteExistingMapping()
            throws Exception
    {
        RestAssuredMockMvc.when()
                          .delete("/configuration/ldap/rolesMapping/Contributors")
                          .peek()
                          .then()
                          .statusCode(200);
    }

    @Test
    public void shouldNotAllowToDeleteNotExistingMapping()
            throws Exception
    {
        RestAssuredMockMvc.when()
                          .delete("/configuration/ldap/rolesMapping/Testers")
                          .peek()
                          .then()
                          .statusCode(400);
    }

    @Test
    public void userSearchFilterShouldBeUpdatable()
            throws Exception
    {
        RestAssuredMockMvc.given()
                          // below line is a workaround URI placeholders in RestAssuredMockMvc
                          .put("/configuration/ldap/userSearchFilter/ou=guys/(uid={0})", "{0}")
                          .peek()
                          .then()
                          .body(CoreMatchers.equalTo("User search filter updated."))
                          .statusCode(200);

        RestAssuredMockMvc.when()
                          .get("/configuration/ldap/userSearchFilter")
                          .peek()
                          .then()
                          .body("userSearch.searchFilter",
                                CoreMatchers.equalTo("(uid={0})"))
                          .body("userSearch.searchBase",
                                CoreMatchers.equalTo("ou=guys"))
                          .statusCode(200);
    }

    @Test
    public void groupSearchFilterShouldBeUpdatable()
            throws Exception
    {
        RestAssuredMockMvc.given()
                          // below line is a workaround URI placeholders in RestAssuredMockMvc
                          .put("/configuration/ldap/groupSearchFilter/ou=guys/(participiant={0})", "{0}")
                          .peek()
                          .then()
                          .body(CoreMatchers.equalTo("Group search filter updated."))
                          .statusCode(200);

        RestAssuredMockMvc.when()
                          .get("/configuration/ldap/groupSearchFilter")
                          .peek()
                          .then()
                          .body("groupSearch.searchBase",
                                CoreMatchers.equalTo("ou=guys"))
                          .body("groupSearch.searchFilter",
                                CoreMatchers.equalTo("(participiant={0})"))
                          .statusCode(200);
    }

    @Test
    public void userDnPatternsAreRemovable()
            throws Exception
    {
        RestAssuredMockMvc.given()
                          // below line is a workaround URI placeholders in RestAssuredMockMvc
                          .delete("/configuration/ldap/userDnPatterns/uid={0},ou=Users", "{0}")
                          .peek()
                          .then()
                          .body(CoreMatchers.equalTo(
                                  "user DN pattern uid={0},ou=Users removed from the userDnPatterns"))
                          .statusCode(200);
    }

    @Test
    public void userDnPatternsAreAdditable()
            throws Exception
    {
        RestAssuredMockMvc.given()
                          // below line is a workaround URI placeholders in RestAssuredMockMvc
                          .post("/configuration/ldap/userDnPatterns/uid={0},ou=Guys", "{0}")
                          .peek()
                          .then()
                          .body(CoreMatchers.equalTo(
                                  "user DN pattern uid={0},ou=Guys added to the userDnPatterns"))
                          .statusCode(200);

        RestAssuredMockMvc.when()
                          .get("/configuration/ldap/userDnPatterns")
                          .peek()
                          .then()
                          .body("userDnPatterns.userDnPattern[0]",
                                CoreMatchers.equalTo("uid={0},ou=Users"))
                          .body("userDnPatterns.userDnPattern[1]",
                                CoreMatchers.equalTo("uid={0},ou=Guys"))
                          .statusCode(200);
    }

}