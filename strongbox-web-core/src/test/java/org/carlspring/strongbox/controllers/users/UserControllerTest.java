package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import io.restassured.http.ContentType;
import org.apache.commons.collections.SetUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class UserControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    UserService userService;

    @Inject
    PasswordEncoder passwordEncoder;

    @After
    public void rollBackAdminUserPassword()
            throws IOException
    {
        User adminUser = retrieveUserByName("admin");
        adminUser.setPassword("password");
        userService.save(adminUser);
    }

    @Test
    public void greetTest()
    {
        displayAllUsers();

        given().contentType(ContentType.JSON)
               .param("name", "Johan")
               .when()
               .get("/users/greet")
               .then()
               .statusCode(200)
               .body(containsString("hello, Johan"));
    }

    @Test
    public void testRetrieveUser()
            throws Exception
    {
        final String userName = "admin";

        given().contentType(ContentType.JSON)
               .param("The name of the user", userName)
               .when()
               .get("/users/user/" + userName)
               .then()
               .statusCode(200)
               .body(containsString("admin"));
    }

    @Test
    public void testCreateUser()
            throws Exception
    {
        UserInput test = buildUser("test", "password");

        given().contentType("application/json")
               .body(test)
               .when()
               .post("/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(200) // check http status code
               .extract()
               .asString();

        displayAllUsers();
    }

    @Test
    public void shouldNotBeAbleToCreateUserWithTheSameUsername()
            throws Exception
    {
        UserInput test = buildUser("test-same-username", "password");

        given().contentType("application/json")
               .body(test)
               .when()
               .post("/users/user");

        try
        {
            given().contentType("application/json")
                   .body(test)
                   .when()
                   .post("/users/user");
        }
        catch (Exception ex)
        {
            assertThat(ex.getCause(), instanceOf(ORecordDuplicatedException.class));
        }

        displayAllUsers();
    }

    @Test
    public void testRetrieveAllUsers()
            throws Exception
    {

        User[] users = given().contentType("application/json")
                              .when()
                              .get("/users/all")
                              .peek() // Use peek() to print the output
                              .as(User[].class);

        assertNotNull(users);
        assertFalse(users.length == 0);

        Stream.of(users).forEach(user -> logger.debug("Retrieved " + user));
    }

    @Test
    // com.orientechnologies.orient.core.storage.ORecordDuplicatedException
    public void testUpdateUser()
            throws Exception
    {
        // create new user
        final String userName = "test-update";
        UserInput test = buildUser(userName, "password-update");

        given().contentType("application/json")
               .body(test)
               .when()
               .post("/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(200) // check http status code
               .extract()
               .asString();

        // retrieve newly created user and store the objectId
        User createdUser = retrieveUserByName(test.getUsername());
        assertNotNull("Created user should have objectId", createdUser.getObjectId());
        assertEquals(userName, createdUser.getUsername());

        // update some property for user
        createdUser.setEnabled(true);

        logger.info("Users before update: ->>>>>> ");
        displayAllUsers();

        // send update request
        String response = given().contentType("application/json")
                                 .body(UserInput.fromUser(createdUser))
                                 .when()
                                 .put("/users/user")
                                 .peek()
                                 .then()
                                 .statusCode(200)
                                 .extract()
                                 .asString();

        assertThat(response, equalTo("The user was updated successfully."));

        logger.info("Users after update: ->>>>>> ");
        displayAllUsers();

        createdUser = retrieveUserByName("test-update");
        assertEquals(true, createdUser.isEnabled());
    }

    @Test
    public void userShouldBeAbleToChangeTheirOwnPassword()
            throws Exception
    {
        final String userName = "admin";
        final String newPassword = "newPassword";
        UserInput admin = buildUser(userName, newPassword);

        given().contentType("application/json")
               .body(admin)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .statusCode(200)
               .extract()
               .asString();

        User updatedUser = retrieveUserByName(admin.getUsername());
        assertEquals(userName, updatedUser.getUsername());
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    @Test
    public void userShouldNotBeAbleToChangeTheirOwnPasswordToNull()
            throws Exception
    {
        final String userName = "admin";
        final String newPassword = null;
        UserInput admin = buildUser(userName, newPassword);

        given().contentType("application/json")
               .body(admin)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .statusCode(200)
               .extract()
               .asString();

        User updatedUser = retrieveUserByName(admin.getUsername());
        assertEquals(userName, updatedUser.getUsername());
        assertNotNull(updatedUser.getPassword());
    }

    @Test
    public void userShouldNotBeAbleToChangeSomeoneElsePasswordToNull()
            throws Exception
    {
        User mavenUser = retrieveUserByName("maven");
        UserInput input = UserInput.fromUser(mavenUser);
        input.setPassword(null);

        given().contentType("application/json")
               .body(input)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .statusCode(200)
               .extract()
               .asString();

        User updatedUser = retrieveUserByName("maven");
        assertNotNull(updatedUser.getPassword());
    }

    @Test
    public void userShouldNotBeAbleToChangeSomeoneElsePasswordToEmpty()
            throws Exception
    {
        User mavenUser = retrieveUserByName("maven");
        UserInput input = UserInput.fromUser(mavenUser);
        input.setPassword("");

        given().contentType("application/json")
               .body(input)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .statusCode(200)
               .extract()
               .asString();

        User updatedUser = retrieveUserByName("maven");
        assertFalse(passwordEncoder.matches("", updatedUser.getPassword()));
    }

    @Test
    public void userShouldNotBeAbleToChangeTheirOwnPasswordToEmpty()
            throws Exception
    {
        final String userName = "admin";
        final String newPassword = "";
        UserInput admin = buildUser(userName, newPassword);

        given().contentType("application/json")
               .body(admin)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .statusCode(200)
               .extract()
               .asString();

        User updatedUser = retrieveUserByName(admin.getUsername());
        assertEquals(userName, updatedUser.getUsername());
        assertFalse(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    @Test
    public void shouldBeAbleToUpdateRoles()
            throws Exception
    {
        final String userName = "maven";
        final String newPassword = "password";
        UserInput admin = buildUser(userName, newPassword);

        User updatedUser = retrieveUserByName(admin.getUsername());
        assertTrue(SetUtils.isEqualSet(updatedUser.getRoles(), ImmutableSet.of("admin")));

        admin.setRoles(ImmutableSet.of("UI_MANAGER"));
        given().contentType("application/json")
               .body(admin)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .statusCode(200)
               .extract()
               .asString();

        updatedUser = retrieveUserByName(admin.getUsername());
        assertTrue(SetUtils.isEqualSet(updatedUser.getRoles(), ImmutableSet.of("UI_MANAGER")));
    }

    private void displayAllUsers()
    {

        // display all current users
        logger.info("All current users:");
        userService.findAll()
                   .ifPresent(users ->
                              {
                                  users.forEach(user -> logger.info(user.toString()));
                              });
    }

    @Test
    @WithUserDetails("developer01")
    public void userWithoutViewUserRoleShouldBeAbleToViewHisAccountData()
            throws Exception
    {
        given().contentType(ContentType.JSON)
               .when()
               .get("/users/user/developer01")
               .then()
               .statusCode(200)
               .body(containsString("developer01"));
    }

    @Test
    @WithUserDetails("deployer")
    public void userWithoutUpdateUserRoleShouldBeAbleToUpdateHisPassword()
            throws Exception
    {
        User updatedUser = retrieveUserByName("deployer");
        String initialPassword = updatedUser.getPassword();

        final String userName = "deployer";
        final String newPassword = "newPassword";
        UserInput developer01 = buildUser(userName, newPassword);

        given().contentType("application/json")
               .body(developer01)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .statusCode(200)
               .extract()
               .asString();

        updatedUser = retrieveUserByName(developer01.getUsername());
        assertEquals(userName, updatedUser.getUsername());
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));

        updatedUser.setPassword(initialPassword);
    }

    @Test
    @WithUserDetails("developer01")
    public void userWithoutViewUserRoleShouldNotBeAbleToViewOtherUserAccountData()
            throws Exception
    {
        given().contentType(ContentType.JSON)
               .when()
               .get("/users/user/admin")
               .then()
               .statusCode(403);
    }

    @Test
    @WithUserDetails("developer01")
    public void userWithoutUpdateUserRoleShouldNotBeAbleToUpdateSomeoneElsePassword()
            throws Exception
    {
        final String userName = "admin";
        final String newPassword = "newPassword";
        UserInput admin = buildUser(userName, newPassword);

        given().contentType("application/json")
               .body(admin)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .statusCode(403);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    // com.orientechnologies.orient.core.storage.ORecordDuplicatedException
    public void testGenerateSecurityToken()
            throws Exception
    {
        UserInput input = buildUser("test-jwt", "password-update");

        //1. Create user
        given().contentType("application/json")
               .body(input)
               .when()
               .post("/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(200) // check http status code
               .extract()
               .asString();

        User user = retrieveUserByName(input.getUsername());
        assertNotNull("Created user should have objectId", user.getObjectId());

        //2. Provide `securityTokenKey`
        user.setSecurityTokenKey("seecret");
        given().contentType("application/json")
               .body(UserInput.fromUser(user))
               .when()
               .put("/users/user")
               .peek();

        user = retrieveUserByName(input.getUsername());
        assertNotNull(user.getSecurityTokenKey());
        assertThat(user.getSecurityTokenKey(), CoreMatchers.equalTo("seecret"));

        //3. Generate token
        String response = given().when()
                                 .get("/users/user/test-jwt/generate-security-token")
                                 .peek()
                                 .then()
                                 .statusCode(200)
                                 .extract()
                                 .asString();
        assertTrue(response.startsWith("eyJhbGciOiJIUzI1NiJ9"));
    }

    @Test
    // com.orientechnologies.orient.core.storage.ORecordDuplicatedException
    public void testDeleteUser()
            throws Exception
    {
        // create new user
        UserInput test = buildUser("test-delete", "password-update");

        given().contentType("application/json")
               .body(test)
               .when()
               .post("/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(200) // check http status code
               .extract()
               .asString();

        given().contentType("application/json")
               .param("The name of the user", test.getUsername())
               .when()
               .delete("/users/user/" + test.getUsername())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(200) // check http status code
               .extract()
               .asString();

    }

    @Test
    public void testUpdateAccessModel()
            throws Exception
    {

        // load user with custom access model
        User developer01 = getUser("developer01");
        AccessModel accessModel = developer01.getAccessModel();
        assertNotNull(accessModel);

        logger.debug(accessModel.toString());
        assertFalse(accessModel.getWildCardPrivilegesMap()
                               .isEmpty());
        assertFalse(accessModel.getRepositoryPrivileges()
                               .isEmpty());

        // modify access model and save it
        final String mockUrl = "/storages/storage0/act-releases-1/pro/redsoft";
        final String mockPrivilege = Privileges.ARTIFACTS_DELETE.toString();
        accessModel.getUrlToPrivilegesMap()
                   .put(mockUrl, Arrays.asList(mockPrivilege));

        User updatedUser = given().contentType("application/json")
                                  .body(accessModel)
                                  .put("/users/user/developer01/access-model")
                                  .peek() // Use peek() to print the output
                                  .then()
                                  .statusCode(200) // check http status code
                                  .extract()
                                  .as(User.class);

        AccessModel updatedModel = updatedUser.getAccessModel();
        assertNotNull(updatedModel);
        logger.debug(updatedModel.toString());

        Collection<String> privileges = updatedModel.getUrlToPrivilegesMap()
                                                    .get(mockUrl);
        assertNotNull(privileges);
        assertTrue(privileges.contains(mockPrivilege));
    }

    // get user through REST API
    private User getUser(String userName)
    {

        return given().contentType(ContentType.JSON)
                      .param("The name of the user", userName)
                      .when()
                      .get("/users/user/" + userName)
                      .then()
                      .statusCode(HttpStatus.OK.value())
                      .extract()
                      .as(User.class);
    }

    // get user from DB/cache directly
    private User retrieveUserByName(String name)
            throws IOException
    {
        return userService.findByUserName(name);
    }

    private UserInput buildUser(String name,
                                String password)
    {
        UserInput test = new UserInput();
        test.setUsername(name);
        test.setPassword(password);
        test.setEnabled(false);

        return test;
    }

}