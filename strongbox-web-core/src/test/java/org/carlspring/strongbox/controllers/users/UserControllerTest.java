package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections.SetUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
@Transactional
public class UserControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    private UserService userService;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private PlatformTransactionManager transactionManager;


    @After
    public void rollBackAdminUserPassword()
    {
        TransactionTemplate t = new TransactionTemplate();
        t.setTransactionManager(transactionManager);
        t.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
        t.execute((s) -> {
            User adminUser = retrieveUserByName("admin");
            adminUser.setPassword("password");
            userService.save(adminUser);
            return null;
        });
    }

    @Test
    public void greetTestWithTextAcceptHeader()
    {
        displayAllUsers();

        String name = "Johan";
        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .param("name", name)
               .when()
               .get("/api/users/greet")
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo("hello, " + name));
    }

    @Test
    public void greetTestWithJsonAcceptHeader()
    {
        displayAllUsers();

        String name = "Johan";
        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .param("name", name)
               .when()
               .get("/api/users/greet")
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("hello, " + name));
    }

    @Test
    public void testRetrieveUserWithTextAcceptHeader()
    {
        final String username = "admin";

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .param("The name of the user", username)
               .when()
               .get("/api/users/user/" + username)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(username));
    }

    @Test
    public void testRetrieveUserWithJsonAcceptHeader()
    {
        final String username = "admin";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .param("The name of the user", username)
               .when()
               .get("/api/users/user/" + username)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("username", equalTo(username));
    }

    @Test
    public void shouldNotBeAbleToRetrieveUserThatNoExists()
    {
        final String username = "userNotFound";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .param("The name of the user", username)
               .when()
               .get("/api/users/user/" + username)
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body("message", equalTo("The specified user does not exist!"));
    }

    @Test
    public void testCreateUser()
    {
        UserInput test = buildUser("test", "password");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(test)
               .when()
               .post("/api/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The user was created successfully."))
               .extract()
               .asString();

        displayAllUsers();
    }

    @Test
    public void shouldNotBeAbleToCreateUserWithTheSameUsername()
    {
        UserInput test = buildUser("test-same-username", "password");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(test)
               .when()
               .post("/api/users/user")
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The user was created successfully."));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(test)
               .when()
               .post("/api/users/user")
               .then()
               .statusCode(HttpStatus.CONFLICT.value())
               .body("message", equalTo("A user with this username already exists! Please enter another username."));

        displayAllUsers();
    }

    @Test
    public void testRetrieveAllUsers()
    {

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/api/users/all")
               .peek() // Use peek() to print the output
               .then()
               .body("users", hasSize(greaterThan(0)))
               .statusCode(200);
    }

    @Test
    public void testUpdateUser()
    {
        // create new user
        final String username = "test-update";
        UserInput test = buildUser(username, "password-update");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(test)
               .when()
               .post("/api/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The user was created successfully."))
               .extract()
               .asString();

        // retrieve newly created user and store the objectId
        User createdUser = retrieveUserByName(test.getUsername());
        assertNotNull("Created user should have objectId", createdUser.getObjectId());
        assertEquals(username, createdUser.getUsername());

        // update some property for user
        createdUser.setEnabled(true);

        logger.info("Users before update: ->>>>>> ");
        displayAllUsers();

        // send update request
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(UserInput.fromUser(createdUser))
               .when()
               .put("/api/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The user was updated successfully."));

        logger.info("Users after update: ->>>>>> ");
        displayAllUsers();

        createdUser = retrieveUserByName("test-update");

        assertEquals(true, createdUser.isEnabled());
    }

    @Test
    public void userWithoutUsernameShouldNotBeAbleToUpdate()
    {
        final String username = "";
        final String newPassword = "newPassword";
        UserInput admin = buildUser(username, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(admin)
               .when()
               .put("/api/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("message", equalTo("Username not provided."));
    }

    @Test
    public void userShouldBeAbleToChangeTheirOwnPassword()
    {
        final String username = "admin";
        final String newPassword = "newPassword";
        UserInput admin = buildUser(username, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(admin)
               .when()
               .put("/api/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The user was updated successfully."))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName(admin.getUsername());
        assertEquals(username, updatedUser.getUsername());
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    @Test
    public void userShouldNotBeAbleToChangeTheirOwnPasswordToNull()
    {
        final String username = "admin";
        final String newPassword = null;
        UserInput admin = buildUser(username, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(admin)
               .when()
               .put("/api/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The user was updated successfully."))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName(admin.getUsername());
        assertEquals(username, updatedUser.getUsername());
        assertNotNull(updatedUser.getPassword());
    }

    @Test
    public void userShouldNotBeAbleToChangeSomeoneElsePasswordToNull()
    {
        User mavenUser = retrieveUserByName("maven");
        UserInput input = UserInput.fromUser(mavenUser);
        input.setPassword(null);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(input)
               .when()
               .put("/api/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The user was updated successfully."))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName("maven");

        assertNotNull(updatedUser.getPassword());
    }

    @Test
    public void userShouldNotBeAbleToChangeSomeoneElsePasswordToEmpty()
    {
        User mavenUser = retrieveUserByName("maven");
        UserInput input = UserInput.fromUser(mavenUser);
        input.setPassword("");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(input)
               .when()
               .put("/api/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The user was updated successfully."))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName("maven");
        assertFalse(passwordEncoder.matches("", updatedUser.getPassword()));
    }

    @Test
    public void userShouldNotBeAbleToChangeTheirOwnPasswordToEmpty()
    {
        final String username = "admin";
        final String newPassword = "";
        UserInput admin = buildUser(username, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(admin)
               .when()
               .put("/api/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The user was updated successfully."))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName(admin.getUsername());

        assertEquals(username, updatedUser.getUsername());
        assertFalse(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    @Test
    public void shouldBeAbleToUpdateRoles()
    {
        final String username = "maven";
        final String newPassword = "password";
        UserInput admin = buildUser(username, newPassword);

        User updatedUser = retrieveUserByName(admin.getUsername());

        assertTrue(SetUtils.isEqualSet(updatedUser.getRoles(), ImmutableSet.of("admin")));

        admin.setRoles(ImmutableSet.of("UI_MANAGER"));
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(admin)
               .when()
               .put("/api/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The user was updated successfully."))
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
                   .ifPresent(users -> users.forEach(user -> logger.info(user.toString())));
    }

    @Test
    @WithUserDetails("developer01")
    public void userWithoutViewUserRoleShouldBeAbleToViewHisAccountData()
    {
        String username = "developer01";
        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/api/users/user/" + username)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("username", equalTo(username));
    }

    @Test
    @WithUserDetails("deployer")
    public void userWithoutUpdateUserRoleShouldBeAbleToUpdateHisPassword()
    {
        User updatedUser = retrieveUserByName("deployer");
        String initialPassword = updatedUser.getPassword();

        final String username = "deployer";
        final String newPassword = "newPassword";
        UserInput developer01 = buildUser(username, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(developer01)
               .when()
               .put("/api/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The user was updated successfully."))
               .extract()
               .asString();

        updatedUser = retrieveUserByName(developer01.getUsername());
        assertEquals(username, updatedUser.getUsername());
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));

        updatedUser.setPassword(initialPassword);
    }

    @Test
    @WithUserDetails("developer01")
    public void userWithoutViewUserRoleShouldNotBeAbleToViewOtherUserAccountData()
    {
        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/api/users/user/admin")
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @WithUserDetails("developer01")
    public void userWithoutUpdateUserRoleShouldNotBeAbleToUpdateSomeoneElsePassword()
    {
        final String username = "admin";
        final String newPassword = "newPassword";
        UserInput admin = buildUser(username, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(admin)
               .when()
               .put("/api/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testGenerateSecurityToken()
    {
        UserInput input = buildUser("test-jwt", "password-update");

        //1. Create user
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(input)
               .when()
               .post("/api/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The user was created successfully."))
               .extract()
               .asString();

        User user = retrieveUserByName(input.getUsername());
        assertNotNull("Created user should have objectId", user.getObjectId());

        //2. Provide `securityTokenKey`
        user.setSecurityTokenKey("seecret");
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(UserInput.fromUser(user))
               .when()
               .put("/api/users/user")
               .peek();

        user = retrieveUserByName(input.getUsername());
        assertNotNull(user.getSecurityTokenKey());
        assertThat(user.getSecurityTokenKey(), equalTo("seecret"));

        //3. Generate token
        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/api/users/user/test-jwt/generate-security-token")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("token", startsWith("eyJhbGciOiJIUzI1NiJ9"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void userWithoutSecurityTokenKeyShouldNotGenerateSecurityToken()
    {
        String username = "test-jwt-key";
        String password = "password-update";
        UserInput input = buildUser(username, password);

        //1. Create user
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(input)
               .when()
               .post("/api/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The user was created successfully."))
               .extract()
               .asString();

        User user = retrieveUserByName(input.getUsername());
        assertNotNull("Created user should have objectId", user.getObjectId());

        //2. Provide `securityTokenKey` to null
        user.setSecurityTokenKey(null);
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(UserInput.fromUser(user))
               .when()
               .put("/api/users/user")
               .peek();

        user = retrieveUserByName(input.getUsername());
        assertNull(user.getSecurityTokenKey());

        //3. Generate token
        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/api/users/user/" + input.getUsername() + "/generate-security-token")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("message", equalTo("Failed to generate SecurityToken, probably you should first set " +
                                        "SecurityTokenKey for the user: user-[" + input.getUsername() + "]"));
    }

    @Test
    public void testDeleteUser()
    {
        // create new user
        UserInput test = buildUser("test-delete", "password-update");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(test)
               .when()
               .post("/api/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The user was created successfully."));

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .param("The name of the user", test.getUsername())
               .when()
               .delete("/api/users/user/" + test.getUsername())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The user was deleted."));

    }

    @Test
    @WithUserDetails("admin")
    public void userShouldNotBeAbleToDeleteHimself()
    {
        // create new user
        UserInput test = buildUser("admin", "password-update");

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .param("The name of the user", test.getUsername())
               .when()
               .delete("/api/users/user/" + test.getUsername())
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("message", equalTo("Unable to delete yourself"));
    }

    @Test
    @WithUserDetails("admin")
    public void testUpdateAccessModel()
    {
        String username = "test_" + System.currentTimeMillis();

        UserInput test = buildUser(username, "password");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(test)
               .when()
               .post("/api/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The user was created successfully."))
               .extract()
               .asString();

        displayAllUsers();

        // load user with custom access model
        User user = getUser(username);
        AccessModel accessModel = user.getAccessModel();

        assertNotNull(accessModel);

        logger.debug(accessModel.toString());

        assertFalse(accessModel.getWildCardPrivilegesMap().isEmpty());
        assertFalse(accessModel.getRepositoryPrivileges().isEmpty());

        // modify access model and save it
        final String mockUrl = "/storages/storage0/act-releases-1/org/carlspring/strongbox";
        final String mockPrivilege = Privileges.ARTIFACTS_DELETE.toString();
        accessModel.getUrlToPrivilegesMap().put(mockUrl, Collections.singletonList(mockPrivilege));

        User updatedUser = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                                  .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                                  .body(accessModel)
                                  .put("/api/users/user/" + username + "/access-model")
                                  .peek() // Use peek() to print the output
                                  .then()
                                  .statusCode(HttpStatus.OK.value()) // check http status code
                                  .extract()
                                  .as(User.class);

        AccessModel updatedModel = updatedUser.getAccessModel();
        assertNotNull(updatedModel);

        logger.debug(updatedModel.toString());

        Collection<String> privileges = updatedModel.getUrlToPrivilegesMap().get(mockUrl);

        assertNotNull(privileges);
        assertTrue(privileges.contains(mockPrivilege));
    }

    @Test
    public void userNotExistingShouldNotUpdateAccessModel()
    {
        // load user with custom access model
        User test = getUser("developer01");
        AccessModel accessModel = test.getAccessModel();

        assertNotNull(accessModel);

        logger.debug(accessModel.toString());

        assertFalse(accessModel.getWildCardPrivilegesMap().isEmpty());
        assertFalse(accessModel.getRepositoryPrivileges().isEmpty());

        // modify access model and save it
        final String mockUrl = "/api/storages/storage0/act-releases-1/org/carlspring/strongbox";
        final String mockPrivilege = Privileges.ARTIFACTS_DELETE.toString();

        accessModel.getUrlToPrivilegesMap().put(mockUrl, Collections.singletonList(mockPrivilege));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(accessModel)
               .put("/api/users/user/userNotFound/access-model")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value()) // check http status code
               .body("message", equalTo("The specified user does not exist!"));
    }

    // get user through REST API
    private User getUser(String username)
    {
        return given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                      .param("The name of the user", username)
                      .when()
                      .get("/api/users/user/" + username)
                      .then()
                      .statusCode(HttpStatus.OK.value())
                      .extract()
                      .as(User.class);
    }

    // get user from DB/cache directly
    private User retrieveUserByName(String name)
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
