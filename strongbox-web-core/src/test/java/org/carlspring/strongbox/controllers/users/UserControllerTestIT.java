package org.carlspring.strongbox.controllers.users;

import static org.assertj.core.api.Assertions.assertThat;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.controllers.users.support.UserOutput;
import org.carlspring.strongbox.controllers.users.support.UserResponseEntity;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.SystemRole;
import org.carlspring.strongbox.users.domain.UserData;
import org.carlspring.strongbox.users.dto.User;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.OrientDbUserService.OrientDb;

import javax.inject.Inject;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections4.SetUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.carlspring.strongbox.controllers.users.UserController.FAILED_CREATE_USER;
import static org.carlspring.strongbox.controllers.users.UserController.FAILED_GENERATE_SECURITY_TOKEN;
import static org.carlspring.strongbox.controllers.users.UserController.NOT_FOUND_USER;
import static org.carlspring.strongbox.controllers.users.UserController.OWN_USER_DELETE_FORBIDDEN;
import static org.carlspring.strongbox.controllers.users.UserController.SUCCESSFUL_CREATE_USER;
import static org.carlspring.strongbox.controllers.users.UserController.SUCCESSFUL_DELETE_USER;
import static org.carlspring.strongbox.controllers.users.UserController.SUCCESSFUL_UPDATE_USER;
import static org.carlspring.strongbox.controllers.users.UserController.USER_DELETE_FORBIDDEN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;


/**
 * @author Pablo Tirado
 */
@IntegrationTest
@Transactional
public class UserControllerTestIT
        extends RestAssuredBaseTest
{

    @Inject
    @OrientDb
    private UserService userService;

    @Inject
    private PasswordEncoder passwordEncoder;

    private static Stream<Arguments> usersProvider()
    {
        return Stream.of(
                Arguments.of(MediaType.APPLICATION_JSON_VALUE, "test-create-json"),
                Arguments.of(MediaType.TEXT_PLAIN_VALUE, "test-create-text")
        );
    }

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/api/users");
    }

    @ParameterizedTest
    @MethodSource("usersProvider")
    public void testGetUser(String acceptHeader, String username)
    {
        deleteCreatedUser(username);

        UserDto user = new UserDto();
        user.setEnabled(true);
        user.setUsername(username);
        user.setPassword("test-password");
        user.setSecurityTokenKey("before");

        UserForm userForm = buildFromUser(new UserData(user), u -> u.setEnabled(true));

        // create new user
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(userForm)
               .when()
               .put(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_CREATE_USER))
               .extract()
               .asString();

        // retrieve newly created user and store the objectId
        User  createdUser = retrieveUserByName(user.getUsername());
        assertThat(createdUser.getUsername()).isEqualTo(username);
        assertThat(passwordEncoder.matches(user.getPassword(), createdUser.getPassword())).isTrue();
        assertThat(createdUser.getPassword()).isNotEqualTo(user.getPassword());

        // By default assignableRoles should not present in the response.
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/{name}", username)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("user.username", equalTo(username))
               .body("user.roles", notNullValue())
               .body("user.roles", hasSize(0))
               .body("assignableRoles", nullValue());

        // assignableRoles should be present only if there is ?assignableRoles=true in the request.
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/{name}?formFields=true", username)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("user.username", equalTo(username))
               .body("user.roles", notNullValue())
               .body("user.roles", hasSize(0))
               .body("assignableRoles", notNullValue())
               .body("assignableRoles", hasSize(greaterThan(0)));

        deleteCreatedUser(username);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testUserNotFound(String acceptHeader)
    {
        final String username = "userNotFound";

        mockMvc.accept(acceptHeader)
               .when()
               .get(getContextBaseUrl() + "/{name}", username)
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(containsString(NOT_FOUND_USER));
    }

    @ParameterizedTest
    @MethodSource("usersProvider")
    void createUser(String acceptHeader,
                    String username)
    {
        deleteCreatedUser(username);
        UserForm test = buildUser(username, "password");

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
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

        deleteCreatedUser(username);
    }

    @ParameterizedTest
    @MethodSource("usersProvider")
    void creatingUserWithExistingUsernameShouldFail(String acceptHeader,
                                                    String username)
    {
        deleteCreatedUser(username);
        UserForm test = buildUser(username, "password");

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(test)
               .when()
               .put(getContextBaseUrl())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_CREATE_USER));

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(test)
               .when()
               .put(getContextBaseUrl())
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_CREATE_USER));

        deleteCreatedUser(username);
    }

    @Test
    public void testRetrieveAllUsers()
    {
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .body("users", hasSize(greaterThan(0)))
               .statusCode(HttpStatus.OK.value());
    }

    @ParameterizedTest
    @MethodSource("usersProvider")
    void updateUser(String acceptHeader,
                    String username)
    {
        deleteCreatedUser(username);
        // create new user
        UserForm test = buildUser(username, "password-update", "my-new-security-token");

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
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
        assertThat(createdUser.getUsername()).isEqualTo(username);

        UserForm updatedUser = buildFromUser(createdUser, u -> {
            u.setEnabled(true);
            u.setPassword("new-updated-password");
        });

        // send update request
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(updatedUser)
               .when()
               .put(getContextBaseUrl() + "/" + username)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_UPDATE_USER));

        createdUser = retrieveUserByName(username);

        assertThat(createdUser.isEnabled()).isTrue();
        assertThat(createdUser.getSecurityTokenKey()).isEqualTo("my-new-security-token");
        assertThat(passwordEncoder.matches("new-updated-password", createdUser.getPassword())).isTrue();
        assertThat(createdUser.getPassword()).isNotEqualTo("new-updated-password");

        deleteCreatedUser(username);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void updateExistingUserWithNullPassword(String acceptHeader)
    {
        final String username = "existing-user-with-null-password";
        deleteCreatedUser(username);
        // create new user
        UserForm test = buildUser(username, "password-update", "my-new-security-token");

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
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
        assertThat(createdUser.getUsername()).isEqualTo(username);

        User user = retrieveUserByName(username);
        UserForm input = buildFromUser(user, null);
        input.setPassword(null);

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(input)
               .when()
               .put(getContextBaseUrl() + "/" + user.getUsername())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_UPDATE_USER))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName(username);

        assertThat(updatedUser.getPassword()).isNotNull();
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());

        deleteCreatedUser(username);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void createNewUserWithNullPassword(String acceptHeader)
    {
        UserDto newUserDto = new UserDto();
        newUserDto.setUsername("new-username-with-null-password");

        User newUser = new UserData(newUserDto);
        UserForm input = buildFromUser(newUser, null);
        input.setPassword(null);

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(input)
               .when()
               .put(getContextBaseUrl())
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_CREATE_USER))
               .extract()
               .asString();

        User databaseCheck = retrieveUserByName(newUserDto.getUsername());

        assertThat(databaseCheck).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void setBlankPasswordExistingUser(String acceptHeader)
    {
        UserDto newUserDto = new UserDto();
        newUserDto.setUsername("new-username-with-blank-password");

        User newUser = new UserData(newUserDto);
        UserForm input = buildFromUser(newUser, null);
        input.setPassword("         ");

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(input)
               .when()
               .put(getContextBaseUrl())
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_CREATE_USER))
               .extract()
               .asString();

        User databaseCheck = retrieveUserByName(newUserDto.getUsername());

        assertThat(databaseCheck).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void changeOwnUser(String acceptHeader)
    {
        final String username = "admin";
        final String newPassword = "";
        UserForm user = buildUser(username, newPassword);

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(user)
               .when()
               .put(getContextBaseUrl() + "/{username}", username)
               .peek()
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value())
               .body(containsString(OWN_USER_DELETE_FORBIDDEN))
               .extract()
               .asString();

        User updatedUser = retrieveUserByName(user.getUsername());

        assertThat(updatedUser.getUsername()).isEqualTo(username);
        assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void shouldBeAbleToUpdateRoles(String acceptHeader)
    {
        final String username = "test-user";
        final String newPassword = "password";

        UserDto user = new UserDto();
        user.setEnabled(true);
        user.setUsername(username);
        user.setPassword(newPassword);
        user.setSecurityTokenKey("some-security-token");
        user.setRoles(ImmutableSet.of(SystemRole.UI_MANAGER.name()));
        userService.save(user);

        UserForm admin = buildUser(username, newPassword);

        User updatedUser = retrieveUserByName(admin.getUsername());

        assertThat(SetUtils.isEqualSet(updatedUser.getRoles(), ImmutableSet.of(SystemRole.UI_MANAGER.name()))).isTrue();

        admin.setRoles(ImmutableSet.of("ADMIN"));
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
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

        assertThat(SetUtils.isEqualSet(updatedUser.getRoles(), ImmutableSet.of("ADMIN"))).isTrue();

        // Rollback changes.
        admin.setRoles(ImmutableSet.of(SystemRole.UI_MANAGER.name()));
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(admin)
               .when()
               .put(getContextBaseUrl() + "/{username}", username)
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    @Test
    @WithUserDetails("deployer")
    public void testUserWithoutViewUserRoleShouldNotBeAbleToViewUserAccountData()
    {
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/admin")
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value());

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/deployer")
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @WithUserDetails("deployer")
    public void testUserWithoutUpdateUserRoleShouldNotBeAbleToUpdateSomeoneElsePassword()
    {
        final String username = "admin";
        final String newPassword = "newPassword";
        UserForm admin = buildUser(username, newPassword);

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
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
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
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
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(updatedUser)
               .when()
               .put(getContextBaseUrl() + "/{username}", username)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_UPDATE_USER));

        user = retrieveUserByName(input.getUsername());
        assertThat(user.getSecurityTokenKey()).isNotNull();
        assertThat(user.getSecurityTokenKey()).isEqualTo("seecret");

        //3. Generate token
        try {
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/{username}/generate-security-token", username)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("token", startsWith("eyJhbGciOiJIUzI1NiJ9"));
        }
        finally
        {
            deleteCreatedUser(username);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testUserWithoutSecurityTokenKeyShouldNotGenerateSecurityToken()
    {
        String username = "test-jwt-key";
        String password = "password-update";
        UserForm input = buildUser(username, password);

        //1. Create user
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
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

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               //2. Provide `securityTokenKey` to null
               .body(buildFromUser(user, u -> u.setSecurityTokenKey(null)))
               .when()
               .put(getContextBaseUrl())
               .peek();

        user = retrieveUserByName(input.getUsername());
        assertThat(user.getSecurityTokenKey()).isNull();

        //3. Generate token
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/{username}/generate-security-token", input.getUsername())
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("message", containsString(FAILED_GENERATE_SECURITY_TOKEN));

        deleteCreatedUser(username);
    }

    @Test
    public void testDeleteUser()
    {
        // create new user
        UserForm userForm = buildUser("test-delete", "password-update");

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(userForm)
               .when()
               .put(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_CREATE_USER));

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
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
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(getContextBaseUrl() + "/{username}", "test-deleting-own-user")
               .peek()
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value())
               .body(containsString(OWN_USER_DELETE_FORBIDDEN));
    }

    @Disabled // disabled temporarily
    @Test
    @WithMockUser(username = "another-admin", authorities = "DELETE_USER")
    public void testDeletingRootAdminShouldBeForbidden()
    {
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(getContextBaseUrl() + "/{username}", "admin")
               .peek()
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value())
               .body(containsString(USER_DELETE_FORBIDDEN));
    }

    @ParameterizedTest
    @MethodSource("usersProvider")
    public void testNotValidMapsShouldNotUpdateAccessModel(String acceptHeader, String username)
    {
        deleteCreatedUser(username);

        UserDto user = new UserDto();
        user.setEnabled(true);
        user.setUsername(username);
        user.setPassword("test-password");
        user.setSecurityTokenKey("before");

        UserForm userForm = buildFromUser(new UserData(user), u -> u.setEnabled(true));

        // create new user
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(acceptHeader)
                .body(userForm)
                .when()
                .put(getContextBaseUrl())
                .peek() // Use peek() to print the output
                .then()
                .statusCode(HttpStatus.OK.value()) // check http status code
                .body(containsString(SUCCESSFUL_CREATE_USER))
                .extract()
                .asString();

        // retrieve newly created user and store the objectId
        User createdUser = retrieveUserByName(user.getUsername());
        assertThat(createdUser.getUsername()).isEqualTo(username);

        deleteCreatedUser(username);
    }

    private void deleteCreatedUser(String username)
    {
        logger.debug("Delete created user: {}", username);
        userService.deleteByUsername(username);
    }

    // get user through REST API
    private UserOutput getUser(String username)
    {
        UserResponseEntity responseEntity = mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
                                                   .param("The name of the user", username)
                                                   .when()
                                                   .get(getContextBaseUrl() + "/{username}", username)
                                                   .then()
                                                   .statusCode(HttpStatus.OK.value())
                                                   .extract()
                                                   .as(UserResponseEntity.class);

        return responseEntity.getUser();
    }

    // get user from DB/cache directly
    private User retrieveUserByName(String name)
    {
        return userService.findByUsername(name);
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
        dto.setSecurityTokenKey(user.getSecurityTokenKey());

        if (operation != null)
        {
            operation.accept(dto);
        }

        return dto;
    }

}
