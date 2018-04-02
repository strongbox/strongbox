package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.users.AccessModelForm;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.collections.SetUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import static org.carlspring.strongbox.controllers.users.UserController.*;
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
public class UserControllerTestIT
        extends RestAssuredBaseTest
{

    @Inject
    private UserService userService;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private PlatformTransactionManager transactionManager;

    @Override
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/api/users");
    }

    @After
    public void rollBackAdminUserPassword()
    {
        TransactionTemplate t = new TransactionTemplate();
        t.setTransactionManager(transactionManager);
        t.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
        t.execute(s -> {
            User adminUser = retrieveUserByName("admin");
            adminUser.setPassword("password");
            userService.save(adminUser);
            return null;
        });
    }

    private void greetTest(String acceptHeader)
    {
        displayAllUsers();

        String name = "Johan";
        given().accept(acceptHeader)
               .param("name", name)
               .when()
               .get(getContextBaseUrl() + "/greet")
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString("hello, " + name));
    }

    @Test
    public void greetTestWithTextAcceptHeader()
    {
        greetTest(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void greetTestWithJsonAcceptHeader()
    {
        greetTest(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void testGetUser()
    {
        final String username = "admin";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .param("The name of the user", username)
               .when()
               .get(getContextBaseUrl() + "/user/{name}", username)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(username));
    }

    private void shouldNotBeAbleToRetrieveUserThatNoExists(String acceptHeader)
    {
        final String username = "userNotFound";

        given().accept(acceptHeader)
               .param("The name of the user", username)
               .when()
               .get(getContextBaseUrl() + "/user/{name}", username)
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(containsString(NOT_FOUND_USER));
    }

    @Test
    public void shouldNotBeAbleToRetrieveUserThatNoExistsWithTextAcceptHeader()
    {
        shouldNotBeAbleToRetrieveUserThatNoExists(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void shouldNotBeAbleToRetrieveUserThatNoExistsWithJsonAcceptHeader()
    {
        shouldNotBeAbleToRetrieveUserThatNoExists(MediaType.APPLICATION_JSON_VALUE);
    }

    private void testCreateUser(String userName,
                                String acceptHeader)
    {
        UserForm test = buildUser(userName, "password");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(test)
               .when()
               .post(getContextBaseUrl() + "/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_CREATE_USER))
               .extract()
               .asString();

        displayAllUsers();
    }

    @Test
    public void testCreateUserWithJsonAcceptHeader()
    {
        testCreateUser("test-create-json", MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void testCreateUserWithTextAcceptHeader()
    {
        testCreateUser("test-create-text", MediaType.TEXT_PLAIN_VALUE);
    }

    private void shouldNotBeAbleToCreateUserWithTheSameUsername(String userName, String acceptHeader)
    {
        UserForm test = buildUser(userName, "password");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(test)
               .when()
               .post(getContextBaseUrl() + "/user")
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_CREATE_USER));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(test)
               .when()
               .post(getContextBaseUrl() + "/user")
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_CREATE_USER));

        displayAllUsers();
    }

    @Test
    public void shouldNotBeAbleToCreateUserWithTheSameUsernameWithTextAcceptHeader()
    {
        String userName = "test-same-username-text";
        shouldNotBeAbleToCreateUserWithTheSameUsername(userName, MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void shouldNotBeAbleToCreateUserWithTheSameUsernameWithJsonAcceptHeader()
    {
        String userName = "test-same-username-json";
        shouldNotBeAbleToCreateUserWithTheSameUsername(userName, MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void testRetrieveAllUsers()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/all")
               .peek() // Use peek() to print the output
               .then()
               .body("users", hasSize(greaterThan(0)))
               .statusCode(HttpStatus.OK.value());
    }

    private void testUpdateUser(String acceptHeader,
                                String username)
    {
        // create new user
        UserForm test = buildUser(username, "password-update");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(test)
               .when()
               .post(getContextBaseUrl() + "/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_CREATE_USER))
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

        UserForm updatedUser = buildFromUser(createdUser);

        // send update request
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(updatedUser)
               .when()
               .put(getContextBaseUrl() + "/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_UPDATE_USER));

        logger.info("Users after update: ->>>>>> ");
        displayAllUsers();

        createdUser = retrieveUserByName(username);

        assertTrue(createdUser.isEnabled());
    }

    @Test
    public void testUpdateUserWithTextAcceptHeader()
    {
        final String username = "test-update-text";
        testUpdateUser(MediaType.TEXT_PLAIN_VALUE, username);
    }

    @Test
    public void testUpdateUserWithJsonAcceptHeader()
    {
        final String username = "test-update-json";
        testUpdateUser(MediaType.APPLICATION_JSON_VALUE, username);
    }

    private void userWithoutUsernameShouldNotBeAbleToUpdate(String acceptHeader)
    {
        final String username = "";
        final String newPassword = "newPassword";
        UserForm admin = buildUser(username, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(admin)
               .when()
               .put(getContextBaseUrl() + "/user")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_UPDATE_USER));
    }

    @Test
    public void userWithoutUsernameShouldNotBeAbleToUpdateWithJsonAcceptHeader()
    {
        userWithoutUsernameShouldNotBeAbleToUpdate(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void userWithoutUsernameShouldNotBeAbleToUpdateWithTextAcceptHeader()
    {
        userWithoutUsernameShouldNotBeAbleToUpdate(MediaType.TEXT_PLAIN_VALUE);
    }

    private void userShouldBeAbleToChangeTheirOwnPassword(String acceptHeader)
    {
        final String username = "admin";
        final String newPassword = "newPassword";
        UserForm admin = buildUser(username, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(admin)
               .when()
               .put(getContextBaseUrl() + "/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_UPDATE_USER))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName(admin.getUsername());
        assertEquals(username, updatedUser.getUsername());
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    @Test
    public void userShouldBeAbleToChangeTheirOwnPasswordWithTextAcceptHeader()
    {
        userShouldBeAbleToChangeTheirOwnPassword(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void userShouldBeAbleToChangeTheirOwnPasswordWithJsonAcceptHeader()
    {
        userShouldBeAbleToChangeTheirOwnPassword(MediaType.APPLICATION_JSON_VALUE);
    }

    private void userShouldNotBeAbleToChangeTheirOwnPasswordToNull(String acceptHeader)
    {
        final String username = "admin";
        final String newPassword = null;
        UserForm admin = buildUser(username, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(admin)
               .when()
               .put(getContextBaseUrl() + "/user")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_UPDATE_USER))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName(admin.getUsername());
        assertEquals(username, updatedUser.getUsername());
        assertNotNull(updatedUser.getPassword());
    }

    @Test
    public void userShouldNotBeAbleToChangeTheirOwnPasswordToNullWithTextAcceptHeader()
    {
        userShouldNotBeAbleToChangeTheirOwnPasswordToNull(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void userShouldNotBeAbleToChangeTheirOwnPasswordToNullWithJsonAcceptHeader()
    {
        userShouldNotBeAbleToChangeTheirOwnPasswordToNull(MediaType.APPLICATION_JSON_VALUE);
    }

    private void userShouldNotBeAbleToChangeSomeoneElsePasswordToNull(String acceptHeader)
    {
        User mavenUser = retrieveUserByName("maven");
        UserForm input = buildFromUser(mavenUser);
        input.setPassword(null);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(input)
               .when()
               .put(getContextBaseUrl() + "/user")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_UPDATE_USER))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName("maven");

        assertNotNull(updatedUser.getPassword());
    }

    @Test
    public void userShouldNotBeAbleToChangeSomeoneElsePasswordToNullWithTextAcceptHeader()
    {
        userShouldNotBeAbleToChangeSomeoneElsePasswordToNull(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void userShouldNotBeAbleToChangeSomeoneElsePasswordToNullWithJsonAcceptHeader()
    {
        userShouldNotBeAbleToChangeSomeoneElsePasswordToNull(MediaType.APPLICATION_JSON_VALUE);
    }

    private void userShouldNotBeAbleToChangeSomeoneElsePasswordToEmpty(String acceptHeader)
    {
        User mavenUser = retrieveUserByName("maven");
        UserForm input = buildFromUser(mavenUser);
        input.setPassword("");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(input)
               .when()
               .put(getContextBaseUrl() + "/user")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_UPDATE_USER))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName("maven");
        assertFalse(passwordEncoder.matches("", updatedUser.getPassword()));
    }

    @Test
    public void userShouldNotBeAbleToChangeSomeoneElsePasswordToEmptyWithTextAcceptHeader()
    {
        userShouldNotBeAbleToChangeSomeoneElsePasswordToEmpty(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void userShouldNotBeAbleToChangeSomeoneElsePasswordToEmptyWithJsonAcceptHeader()
    {
        userShouldNotBeAbleToChangeSomeoneElsePasswordToEmpty(MediaType.APPLICATION_JSON_VALUE);
    }

    private void userShouldNotBeAbleToChangeTheirOwnPasswordToEmpty(String acceptHeader)
    {
        final String username = "admin";
        final String newPassword = "";
        UserForm admin = buildUser(username, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(admin)
               .when()
               .put(getContextBaseUrl() + "/user")
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_UPDATE_USER))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName(admin.getUsername());

        assertEquals(username, updatedUser.getUsername());
        assertFalse(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    @Test
    public void userShouldNotBeAbleToChangeTheirOwnPasswordToEmptyWithTextAcceptHeader()
    {
        userShouldNotBeAbleToChangeTheirOwnPasswordToEmpty(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void userShouldNotBeAbleToChangeTheirOwnPasswordToEmptyWithJsonAcceptHeader()
    {
        userShouldNotBeAbleToChangeTheirOwnPasswordToEmpty(MediaType.APPLICATION_JSON_VALUE);
    }

    private void shouldBeAbleToUpdateRoles(String acceptHeader)
    {
        final String username = "maven";
        final String newPassword = "password";
        UserForm admin = buildUser(username, newPassword);

        User updatedUser = retrieveUserByName(admin.getUsername());

        assertTrue(SetUtils.isEqualSet(updatedUser.getRoles(), ImmutableSet.of("admin")));

        admin.setRoles(ImmutableSet.of("UI_MANAGER"));
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(admin)
               .when()
               .put(getContextBaseUrl() + "/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_UPDATE_USER))
               .extract()
               .asString();

        updatedUser = retrieveUserByName(admin.getUsername());

        assertTrue(SetUtils.isEqualSet(updatedUser.getRoles(), ImmutableSet.of("UI_MANAGER")));

        // Rollback changes.
        admin.setRoles(ImmutableSet.of("admin"));
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(admin)
               .when()
               .put(getContextBaseUrl() + "/user")
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void shouldBeAbleToUpdateRolesWithTextAcceptHeader()
    {
        shouldBeAbleToUpdateRoles(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void shouldBeAbleToUpdateRolesWithJsonAcceptHeader()
    {
        shouldBeAbleToUpdateRoles(MediaType.APPLICATION_JSON_VALUE);
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
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/user/{name}", username)
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
        UserForm developer01 = buildUser(username, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(developer01)
               .when()
               .put(getContextBaseUrl() + "/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_UPDATE_USER))
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
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/user/admin")
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @WithUserDetails("developer01")
    public void userWithoutUpdateUserRoleShouldNotBeAbleToUpdateSomeoneElsePassword()
    {
        final String username = "admin";
        final String newPassword = "newPassword";
        UserForm admin = buildUser(username, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(admin)
               .when()
               .put(getContextBaseUrl() + "/user")
               .peek()
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testGenerateSecurityToken()
    {
        String userName = "test-jwt";
        UserForm input = buildUser(userName, "password-update");

        //1. Create user
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(input)
               .when()
               .post(getContextBaseUrl() + "/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_CREATE_USER))
               .extract()
               .asString();

        User user = retrieveUserByName(input.getUsername());
        assertNotNull("Created user should have objectId", user.getObjectId());

        UserForm updatedUser = buildFromUser(user);
        updatedUser.setSecurityTokenKey("seecret");

        //2. Provide `securityTokenKey`
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(updatedUser)
               .when()
               .put(getContextBaseUrl() + "/user")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_UPDATE_USER));

        user = retrieveUserByName(input.getUsername());
        assertNotNull(user.getSecurityTokenKey());
        assertThat(user.getSecurityTokenKey(), equalTo("seecret"));

        //3. Generate token
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/user/{username}/generate-security-token", userName)
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
        UserForm input = buildUser(username, password);

        //1. Create user
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(input)
               .when()
               .post(getContextBaseUrl() + "/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_CREATE_USER))
               .extract()
               .asString();

        User user = retrieveUserByName(input.getUsername());
        assertNotNull("Created user should have objectId", user.getObjectId());

        //2. Provide `securityTokenKey` to null
        user.setSecurityTokenKey(null);
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(buildFromUser(user))
               .when()
               .put(getContextBaseUrl() + "/user")
               .peek();

        user = retrieveUserByName(input.getUsername());
        assertNull(user.getSecurityTokenKey());

        //3. Generate token
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/user/{username}/generate-security-token", input.getUsername())
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("message", containsString(FAILED_GENERATE_SECURITY_TOKEN));
    }

    @Test
    public void testDeleteUser()
    {
        // create new user
        UserForm test = buildUser("test-delete", "password-update");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(test)
               .when()
               .post(getContextBaseUrl() + "/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_CREATE_USER));

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .param("The name of the user", test.getUsername())
               .when()
               .delete(getContextBaseUrl() + "/user/{name}", test.getUsername())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_DELETE_USER));

    }

    @Test
    @WithUserDetails("admin")
    public void userShouldNotBeAbleToDeleteHimself()
    {
        // create new user
        UserForm test = buildUser("admin", "password-update");

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .param("The name of the user", test.getUsername())
               .when()
               .delete(getContextBaseUrl() + "/user/{name}", test.getUsername())
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_DELETE_SAME_USER));
    }

    @Test
    @WithUserDetails("admin")
    public void testUpdateAccessModel()
    {
        String username = "test_" + System.currentTimeMillis();

        UserForm test = buildUser(username, "password");
        test.setAccessModel(new AccessModelForm());
        test.getAccessModel().getRepositoryPrivileges().put("/storages/storage0/releases", Lists.newArrayList("ARTIFACTS_RESOLVE"));
        test.getAccessModel().getWildCardPrivilegesMap().put("/storages/storage0/releases/com/mycorp/.*", Lists.newArrayList("ARTIFACTS_RESOLVE"));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(test)
               .when()
               .post(getContextBaseUrl() + "/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_CREATE_USER))
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
                                  .accept(MediaType.APPLICATION_JSON_VALUE)
                                  .body(accessModel)
                                  .put(getContextBaseUrl() + "/user/{username}/access-model", username)
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
    public void notValidMapsShouldNotUpdateAccessModel()
    {
        String userName = "developer01";

        // load user with custom access model
        User test = getUser(userName);
        AccessModelForm accessModel = buildFromAccessModel(test.getAccessModel());

        assertNotNull(accessModel);

        logger.debug(accessModel.toString());

        assertFalse(accessModel.getWildCardPrivilegesMap().isEmpty());
        assertFalse(accessModel.getRepositoryPrivileges().isEmpty());

        // modify access model and save it
        final String mockUrl = "/storagesNotValid/storage0/act-releases-1/org/carlspring/strongbox";

        accessModel.getUrlToPrivilegesMap().put(mockUrl, Collections.emptyList());
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(accessModel)
               .put(getContextBaseUrl() + "/user/{username}/access-model", userName)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_UPDATE_ACCESS_MODEL));
    }

    @Test
    public void userNotExistingShouldNotUpdateAccessModel()
    {
        // load user with custom access model
        User test = getUser("developer01");
        AccessModelForm accessModel = buildFromAccessModel(test.getAccessModel());

        assertNotNull(accessModel);

        logger.debug(accessModel.toString());

        assertFalse(accessModel.getWildCardPrivilegesMap().isEmpty());
        assertFalse(accessModel.getRepositoryPrivileges().isEmpty());

        // modify access model and save it
        final String mockUrl = "/storages/storage0/act-releases-1/org/carlspring/strongbox";
        final String mockPrivilege = Privileges.ARTIFACTS_DELETE.toString();

        accessModel.getUrlToPrivilegesMap().put(mockUrl, Collections.singletonList(mockPrivilege));

        String userName = "userNotFound";
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(accessModel)
               .put(getContextBaseUrl() + "/user/{username}/access-model", userName)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value()) // check http status code
               .body(containsString(NOT_FOUND_USER));
    }

    // get user through REST API
    private User getUser(String username)
    {
        return given().accept(MediaType.APPLICATION_JSON_VALUE)
                      .param("The name of the user", username)
                      .when()
                      .get(getContextBaseUrl() + "/user/{name}", username)
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

    private UserForm buildUser(String name,
                               String password)
    {
        UserForm test = new UserForm();
        test.setUsername(name);
        test.setPassword(password);
        test.setEnabled(false);

        return test;
    }

    private UserForm buildFromUser(User user)
    {
        UserForm dto = new UserForm();
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setEnabled(user.isEnabled());
        dto.setRoles(user.getRoles());
        dto.setAccessModel(buildFromAccessModel(user.getAccessModel()));
        dto.setSecurityTokenKey(user.getSecurityTokenKey());
        return dto;
    }

    private AccessModelForm buildFromAccessModel(AccessModel accessModel)
    {
        AccessModelForm dto = null;
        if (accessModel != null)
        {
            dto = new AccessModelForm();
            dto.setRepositoryPrivileges(accessModel.getRepositoryPrivileges());
            dto.setUrlToPrivilegesMap(accessModel.getUrlToPrivilegesMap());
            dto.setWildCardPrivilegesMap(accessModel.getWildCardPrivilegesMap());
        }
        return dto;
    }

}
