package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import javax.inject.Inject;
import javax.transaction.TransactionManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
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
            throws IOException
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
               .get("/users/greet")
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
               .get("/users/greet")
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("hello, " + name));
    }

    @Test
    public void testRetrieveUserWithTextAcceptHeader()
            throws Exception
    {
        final String userName = "admin";

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .param("The name of the user", userName)
               .when()
               .get("/users/user/" + userName)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(userName));
    }

    @Test
    public void testRetrieveUserWithJsonAcceptHeader()
            throws Exception
    {
        final String userName = "admin";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .param("The name of the user", userName)
               .when()
               .get("/users/user/" + userName)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("username", equalTo(userName));
    }

    @Test
    public void shouldNotBeAbleToRetrieveUserThatNoExists()
            throws Exception
    {
        final String userName = "userNotFound";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .param("The name of the user", userName)
               .when()
               .get("/users/user/" + userName)
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body("message", equalTo("The specified user does not exist!"));
    }

    @Test
    public void testCreateUser()
            throws Exception
    {
        UserInput test = buildUser("test", "password");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(test)
               .when()
               .post("/users/user")
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
            throws Exception
    {
        UserInput test = buildUser("test-same-username", "password");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(test)
               .when()
               .post("/users/user")
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The user was created successfully."));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(test)
               .when()
               .post("/users/user")
               .then()
               .statusCode(HttpStatus.CONFLICT.value())
               .body("message", equalTo("A user with this username already exists! Please enter another username."));

        displayAllUsers();
    }

    @Test
    public void testRetrieveAllUsers()
            throws Exception
    {

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/users/all")
               .peek() // Use peek() to print the output
               .then()
               .body("users", hasSize(greaterThan(0)))
               .statusCode(200);
    }

    @Test
    public void testUpdateUser()
            throws Exception
    {
        // create new user
        final String userName = "test-update";
        UserInput test = buildUser(userName, "password-update");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(test)
               .when()
               .post("/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The user was created successfully."))
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
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(UserInput.fromUser(createdUser))
               .when()
               .put("/users/user")
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
            throws Exception
    {
        final String userName = "";
        final String newPassword = "newPassword";
        UserInput admin = buildUser(userName, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(admin)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("message", equalTo("Username not provided."));
    }

    @Test
    public void userShouldBeAbleToChangeTheirOwnPassword()
            throws Exception
    {
        final String userName = "admin";
        final String newPassword = "newPassword";
        UserInput admin = buildUser(userName, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(admin)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The user was updated successfully."))
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

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(admin)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The user was updated successfully."))
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

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(input)
               .when()
               .put("/users/user")
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
            throws Exception
    {
        User mavenUser = retrieveUserByName("maven");
        UserInput input = UserInput.fromUser(mavenUser);
        input.setPassword("");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(input)
               .when()
               .put("/users/user")
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
            throws Exception
    {
        final String userName = "admin";
        final String newPassword = "";
        UserInput admin = buildUser(userName, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(admin)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The user was updated successfully."))
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
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(admin)
               .when()
               .put("/users/user")
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
            throws Exception
    {
        String userName = "developer01";
        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/users/user/" + userName)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("username", equalTo(userName));
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

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(developer01)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The user was updated successfully."))
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
        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/users/user/admin")
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @WithUserDetails("developer01")
    public void userWithoutUpdateUserRoleShouldNotBeAbleToUpdateSomeoneElsePassword()
            throws Exception
    {
        final String userName = "admin";
        final String newPassword = "newPassword";
        UserInput admin = buildUser(userName, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(admin)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    // com.orientechnologies.orient.core.storage.ORecordDuplicatedException
    public void testGenerateSecurityToken()
            throws Exception
    {
        UserInput input = buildUser("test-jwt", "password-update");

        //1. Create user
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(input)
               .when()
               .post("/users/user")
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
               .put("/users/user")
               .peek();

        user = retrieveUserByName(input.getUsername());
        assertNotNull(user.getSecurityTokenKey());
        assertThat(user.getSecurityTokenKey(), equalTo("seecret"));

        //3. Generate token
        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .when()
               .get("/users/user/test-jwt/generate-security-token")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("token", startsWith("eyJhbGciOiJIUzI1NiJ9"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void userWithoutSecurityTokenKeyShouldNotGenerateSecurityToken()
            throws Exception
    {
        String userName = "test-jwt-key";
        String password = "password-update";
        UserInput input = buildUser(userName, password);

        //1. Create user
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(input)
               .when()
               .post("/users/user")
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
               .put("/users/user")
               .peek();

        user = retrieveUserByName(input.getUsername());
        assertNull(user.getSecurityTokenKey());

        //3. Generate token
        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/users/user/" + input.getUsername() + "/generate-security-token")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("message", equalTo("Failed to generate SecurityToken, probably you should first set " +
                                        "SecurityTokenKey for the user: user-[" + input.getUsername() + "]"));
    }

    @Test
    public void testDeleteUser()
            throws Exception
    {
        // create new user
        UserInput test = buildUser("test-delete", "password-update");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(test)
               .when()
               .post("/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The user was created successfully."));

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .param("The name of the user", test.getUsername())
               .when()
               .delete("/users/user/" + test.getUsername())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The user was deleted."));

    }

    @Test
    @WithUserDetails("admin")
    public void userShouldNotBeAbleToDeleteHimself()
            throws Exception
    {
        // create new user
        UserInput test = buildUser("admin", "password-update");

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .param("The name of the user", test.getUsername())
               .when()
               .delete("/users/user/" + test.getUsername())
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("message", equalTo("Unable to delete yourself"));
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
                   .put(mockUrl, Collections.singletonList(mockPrivilege));

        User updatedUser = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                                  .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                                  .body(accessModel)
                                  .put("/users/user/developer01/access-model")
                                  .peek() // Use peek() to print the output
                                  .then()
                                  .statusCode(HttpStatus.OK.value()) // check http status code
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

    @Test
    public void userNotExistingShouldNotUpdateAccessModel()
            throws Exception
    {

        // load user with custom access model
        User test = getUser("developer01");
        AccessModel accessModel = test.getAccessModel();
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
                   .put(mockUrl, Collections.singletonList(mockPrivilege));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(accessModel)
               .put("/users/user/userNotFound/access-model")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value()) // check http status code
               .body("message", equalTo("The specified user does not exist!"));
    }

    // get user through REST API
    private User getUser(String userName)
    {

        return given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
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
