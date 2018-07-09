package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.users.AccessModelForm;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.collections.SetUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.controllers.users.UserController.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testGetUser()
    {
        final String username = "admin";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/{name}", username)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("username", equalTo(username));
    }

    private void userNotFound(String acceptHeader)
    {
        final String username = "userNotFound";

        given().accept(acceptHeader)
               .when()
               .get(getContextBaseUrl() + "/{name}", username)
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(containsString(NOT_FOUND_USER));
    }

    @Test
    public void testUserNotFoundWithTextAcceptHeader()
    {
        userNotFound(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void testUserNotFoundWithJsonAcceptHeader()
    {
        userNotFound(MediaType.APPLICATION_JSON_VALUE);
    }

    private void createUser(String username,
                            String acceptHeader)
    {
        UserForm test = buildUser(username, "password");

        displayAllUsers();

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(test)
               .when()
               .put(getContextBaseUrl())
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
        createUser("test-create-json", MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void testCreateUserWithTextAcceptHeader()
    {
        createUser("test-create-text", MediaType.TEXT_PLAIN_VALUE);
    }

    private void creatingUserWithExistingUsernameShouldFail(String username,
                                                            String acceptHeader)
    {
        UserForm test = buildUser(username, "password");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(test)
               .when()
               .put(getContextBaseUrl())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_CREATE_USER));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(test)
               .when()
               .put(getContextBaseUrl())
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_CREATE_USER));

        displayAllUsers();
    }

    @Test
    public void testCreatingUserWithExistingUsernameWithTextAcceptHeader()
    {
        String username = "test-same-username-text";
        creatingUserWithExistingUsernameShouldFail(username, MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void testCreatingUserWithExistingUsernameWithJsonAcceptHeader()
    {
        String username = "test-same-username-json";
        creatingUserWithExistingUsernameShouldFail(username, MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void testRetrieveAllUsers()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .body("users", hasSize(greaterThan(0)))
               .statusCode(HttpStatus.OK.value());
    }

    private void updateUser(String acceptHeader,
                            String username)
    {
        // create new user
        UserForm test = buildUser(username, "password-update", "my-new-security-token");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(test)
               .when()
               .put(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_CREATE_USER))
               .extract()
               .asString();

        // retrieve newly created user and store the objectId
        User createdUser = retrieveUserByName(test.getUsername());
        assertEquals(username, createdUser.getUsername());

        logger.info("Users before update: ->>>>>> ");
        displayAllUsers();

        UserForm updatedUser = buildFromUser(createdUser, u -> u.setEnabled(true));

        // send update request
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(updatedUser)
               .when()
               .put(getContextBaseUrl() + "/" + username)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_UPDATE_USER));

        logger.info("Users after update: ->>>>>> ");
        displayAllUsers();

        createdUser = retrieveUserByName(username);

        assertTrue(createdUser.isEnabled());
        assertEquals("my-new-security-token", createdUser.getSecurityTokenKey());
    }

    @Test
    public void testUpdateUserWithTextAcceptHeader()
    {
        final String username = "test-update-text";
        updateUser(MediaType.TEXT_PLAIN_VALUE, username);
    }

    @Test
    public void testUpdateUserWithJsonAcceptHeader()
    {
        final String username = "test-update-json";
        updateUser(MediaType.APPLICATION_JSON_VALUE, username);
    }

    private void setNullPassword(String acceptHeader)
    {
        User mavenUser = retrieveUserByName("maven");
        UserForm input = buildFromUser(mavenUser, null);
        input.setPassword(null);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(input)
               .when()
               .put(getContextBaseUrl() + "/" + mavenUser.getUsername())
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_UPDATE_USER))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName("maven");

        assertNotNull(updatedUser.getPassword());
        assertEquals(mavenUser.getPassword(), updatedUser.getPassword());
    }

    @Test
    public void testSettingNullPasswordShouldFailWithTextAcceptHeader()
    {
        setNullPassword(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void testSettingNullPasswordShouldFailWithJsonAcceptHeader()
    {
        setNullPassword(MediaType.APPLICATION_JSON_VALUE);
    }

    private void setBlankPassword(String acceptHeader)
    {
        User mavenUser = retrieveUserByName("maven");
        UserForm input = buildFromUser(mavenUser, null);
        input.setPassword("          ");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(input)
               .when()
               .put(getContextBaseUrl() + "/" + mavenUser.getUsername())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_UPDATE_USER))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName("maven");

        assertNotNull(updatedUser.getPassword());
        assertEquals(mavenUser.getPassword(), updatedUser.getPassword());
    }

    @Test
    public void testSettingBlankPasswordShouldFailWithTextAcceptHeader()
    {
        setBlankPassword(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void testSettingBlankPasswordShouldFailWithJsonAcceptHeader()
    {
        setBlankPassword(MediaType.APPLICATION_JSON_VALUE);
    }

    private void changeOwnUser(final String username, String acceptHeader)
    {
        final String newPassword = "";
        UserForm user = buildUser(username, newPassword);

        int statusCode = HttpStatus.BAD_REQUEST.value();
        String responseContains = FAILED_UPDATE_USER;

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(user)
               .when()
               .put(getContextBaseUrl() + "/{username}", username)
               .peek()
               .then()
               .statusCode(statusCode)
               .body(containsString(responseContains))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName(user.getUsername());

        assertEquals(username, updatedUser.getUsername());
        assertFalse(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    @Test
    @WithUserDetails("maven")
    public void testChangingOwnUserShouldFailWithTextAcceptHeader()
    {
        changeOwnUser("maven", MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    @WithUserDetails("maven")
    public void testChangingOwnUserShouldFailWithJsonAcceptHeader()
    {
        changeOwnUser("maven", MediaType.APPLICATION_JSON_VALUE);
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
               .put(getContextBaseUrl() + "/{username}", username)
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
               .put(getContextBaseUrl() + "/{username}", username)
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void testShouldBeAbleToUpdateRolesWithTextAcceptHeader()
    {
        shouldBeAbleToUpdateRoles(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void testShouldBeAbleToUpdateRolesWithJsonAcceptHeader()
    {
        shouldBeAbleToUpdateRoles(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @WithUserDetails("developer01")
    public void testUserWithoutViewUserRoleShouldNotBeAbleToViewUserAccountData()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/admin")
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value());

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/developer01")
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @WithUserDetails("developer01")
    public void testUserWithoutUpdateUserRoleShouldNotBeAbleToUpdateSomeoneElsePassword()
    {
        final String username = "admin";
        final String newPassword = "newPassword";
        UserForm admin = buildUser(username, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(admin)
               .when()
               .put(getContextBaseUrl() + "/{username}", username)
               .peek()
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testGenerateSecurityToken()
    {
        String username = "test-jwt";
        UserForm input = buildUser(username, "password-update");

        //1. Create user
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(input)
               .when()
               .put(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_CREATE_USER))
               .extract()
               .asString();

        User user = retrieveUserByName(input.getUsername());

        UserForm updatedUser = buildFromUser(user, null);
        updatedUser.setSecurityTokenKey("seecret");

        //2. Provide `securityTokenKey`
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(updatedUser)
               .when()
               .put(getContextBaseUrl() + "/{username}", username)
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
               .get(getContextBaseUrl() + "/{username}/generate-security-token", username)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("token", startsWith("eyJhbGciOiJIUzI1NiJ9"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testUserWithoutSecurityTokenKeyShouldNotGenerateSecurityToken()
    {
        String username = "test-jwt-key";
        String password = "password-update";
        UserForm input = buildUser(username, password);

        //1. Create user
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(input)
               .when()
               .put(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_CREATE_USER))
               .extract()
               .asString();

        User user = retrieveUserByName(input.getUsername());

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               //2. Provide `securityTokenKey` to null
               .body(buildFromUser(user, u -> u.setSecurityTokenKey(null)))
               .when()
               .put(getContextBaseUrl())
               .peek();

        user = retrieveUserByName(input.getUsername());
        assertNull(user.getSecurityTokenKey());

        //3. Generate token
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/{username}/generate-security-token", input.getUsername())
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("message", containsString(FAILED_GENERATE_SECURITY_TOKEN));
    }

    @Test
    public void testDeleteUser()
    {
        // create new user
        UserForm userForm = buildUser("test-delete", "password-update");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(userForm)
               .when()
               .put(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_CREATE_USER));

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .param("The name of the user", userForm.getUsername())
               .when()
               .delete(getContextBaseUrl() + "/{name}", userForm.getUsername())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_DELETE_USER));

    }

    @Test
    @WithMockUser(username = "test-deleting-own-user", authorities = "DELETE_USER")
    @WithUserDetails("test-deleting-own-user")
    public void testUserShouldNotBeAbleToDeleteHimself()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(getContextBaseUrl() + "/{username}", "test-deleting-own-user")
               .peek()
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value())
               .body(containsString(OWN_USER_DELETE_FORBIDDEN));
    }

    @Ignore // disabled temporarily
    @Test
    @WithMockUser(username = "another-admin", authorities = "DELETE_USER")
    public void testDeletingRootAdminShouldBeForbidden()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(getContextBaseUrl() + "/{username}", "admin")
               .peek()
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value())
               .body(containsString(USER_DELETE_FORBIDDEN));
    }

    @Test
    @WithUserDetails("admin")
    public void testUpdateAccessModel()
    {
        String username = "test_" + System.currentTimeMillis();

        UserForm test = buildUser(username, "password");
        test.setAccessModel(new AccessModelForm());
        test.getAccessModel().getRepositoryPrivileges().put("/storages/storage0/releases",
                                                            Lists.newArrayList("ARTIFACTS_RESOLVE"));
        test.getAccessModel().getWildCardPrivilegesMap().put("/storages/storage0/releases/com/mycorp/.*",
                                                             Lists.newArrayList("ARTIFACTS_RESOLVE"));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(test)
               .when()
               .put(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_CREATE_USER))
               .extract()
               .asString();

        displayAllUsers();

        // load user with custom access model
        UserOutput user = getUser(username);
        AccessModelOutput accessModel = user.getAccessModel();

        assertNotNull(accessModel);

        logger.debug(accessModel.toString());

        assertFalse(accessModel.getWildCardPrivilegesMap().isEmpty());
        assertFalse(accessModel.getRepositoryPrivileges().isEmpty());

        // modify access model and save it
        final String mockUrl = "/storages/storage0/act-releases-1/org/carlspring/strongbox";
        final String mockPrivilege = Privileges.ARTIFACTS_DELETE.toString();
        accessModel.getUrlToPrivilegesMap().put(mockUrl, Collections.singletonList(mockPrivilege));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(accessModel)
               .put(getContextBaseUrl() + "/{username}/access-model", username)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value());

        UserOutput updatedUser = getUser(username);

        AccessModelOutput updatedModel = updatedUser.getAccessModel();
        assertNotNull(updatedModel);

        logger.debug(updatedModel.toString());

        Collection<String> privileges = updatedModel.getUrlToPrivilegesMap().get(mockUrl);

        assertNotNull(privileges);
        assertTrue(privileges.contains(mockPrivilege));
    }

    @Test
    public void testNotValidMapsShouldNotUpdateAccessModel()
    {
        String username = "developer01";

        // load user with custom access model
        UserOutput test = getUser(username);
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
               .put(getContextBaseUrl() + "/{username}/access-model", username)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_UPDATE_ACCESS_MODEL));
    }

    @Test
    public void testUpdatingAccessModelForNonExistingUserShouldFail()
    {
        // load user with custom access model
        UserOutput test = getUser("developer01");
        AccessModelForm accessModel = buildFromAccessModel(test.getAccessModel());

        assertNotNull(accessModel);

        logger.debug(accessModel.toString());

        assertFalse(accessModel.getWildCardPrivilegesMap().isEmpty());
        assertFalse(accessModel.getRepositoryPrivileges().isEmpty());

        // modify access model and save it
        final String mockUrl = "/storages/storage0/act-releases-1/org/carlspring/strongbox";
        final String mockPrivilege = Privileges.ARTIFACTS_DELETE.toString();

        accessModel.getUrlToPrivilegesMap().put(mockUrl, Collections.singletonList(mockPrivilege));

        String username = "userNotFound";
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(accessModel)
               .put(getContextBaseUrl() + "/{username}/access-model", username)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value()) // check http status code
               .body(containsString(NOT_FOUND_USER));
    }

    private void displayAllUsers()
    {
        // display all current users
        logger.info("All current users:");
        userService.findAll()
                   .getUsers()
                   .stream()
                   .forEach(user -> logger.info(user.toString()));
    }

    // get user through REST API
    private UserOutput getUser(String username)
    {
        return given().accept(MediaType.APPLICATION_JSON_VALUE)
                      .param("The name of the user", username)
                      .when()
                      .get(getContextBaseUrl() + "/{username}", username)
                      .then()
                      .statusCode(HttpStatus.OK.value())
                      .extract()
                      .as(UserOutput.class);
    }

    // get user from DB/cache directly
    private User retrieveUserByName(String name)
    {
        return userService.findByUserName(name);
    }

    private UserForm buildUser(String name,
                               String password)
    {
        return buildUser(name, password, null, null);
    }

    private UserForm buildUser(String name,
                               String password,
                               String securityTokenKey)
    {
        return buildUser(name, password, securityTokenKey, null);
    }

    private UserForm buildUser(String name,
                               String password,
                               Set<String> roles)
    {
        return buildUser(name, password, null, roles);
    }

    private UserForm buildUser(String name,
                               String password,
                               String securityTokenKey,
                               Set<String> roles)
    {
        UserForm test = new UserForm();
        test.setUsername(name);
        test.setPassword(password);
        test.setSecurityTokenKey(securityTokenKey);
        test.setRoles(roles);
        test.setEnabled(false);

        return test;
    }

    private UserForm buildFromUser(User user,
                                   Consumer<UserForm> operation)
    {
        UserForm dto = new UserForm();
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setSecurityTokenKey(user.getSecurityTokenKey());
        dto.setEnabled(user.isEnabled());
        dto.setRoles(user.getRoles());
        dto.setAccessModel(buildFromAccessModel(new AccessModelOutput(user.getAccessModel())));
        dto.setSecurityTokenKey(user.getSecurityTokenKey());

        if (operation != null)
        {
            operation.accept(dto);
        }

        return dto;
    }

    private AccessModelForm buildFromAccessModel(AccessModelOutput accessModel)
    {
        AccessModelForm dto = null;
        if (accessModel != null)
        {
            dto = new AccessModelForm();
            dto.setRepositoryPrivileges(new HashMap<>(accessModel.getRepositoryPrivileges()));
            dto.setUrlToPrivilegesMap(new HashMap<>(accessModel.getUrlToPrivilegesMap()));
            dto.setWildCardPrivilegesMap(new HashMap<>(accessModel.getWildCardPrivilegesMap()));
        }
        return dto;
    }

}
