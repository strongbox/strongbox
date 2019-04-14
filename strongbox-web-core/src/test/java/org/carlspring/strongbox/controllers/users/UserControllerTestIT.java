package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.controllers.users.support.AccessModelOutput;
import org.carlspring.strongbox.controllers.users.support.RepositoryAccessModelOutput;
import org.carlspring.strongbox.controllers.users.support.UserOutput;
import org.carlspring.strongbox.controllers.users.support.UserResponseEntity;
import org.carlspring.strongbox.converters.users.AccessModelToAccessModelOutputConverter;
import org.carlspring.strongbox.forms.users.AccessModelForm;
import org.carlspring.strongbox.forms.users.RepositoryAccessModelForm;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.domain.Roles;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserRepositoryDto;
import org.carlspring.strongbox.users.dto.UserStorageDto;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.StrongboxUserService.StrongboxUserServiceQualifier;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.collections.SetUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.controllers.users.UserController.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@Transactional
public class UserControllerTestIT
        extends RestAssuredBaseTest
{

    @Inject
    @StrongboxUserServiceQualifier
    private UserService userService;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private PlatformTransactionManager transactionManager;

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
    public void testGetUser(String acceptHeader)
    {
        String username = "test-user-1";

        deleteCreatedUser(username);

        UserDto user = new UserDto();
        user.setEnabled(true);
        user.setUsername(username);
        user.setPassword("test-password");
        user.setSecurityTokenKey("before");

        UserAccessModelDto userAccessModelDto = new UserAccessModelDto();

        UserRepositoryDto userRepositoryDto = new UserRepositoryDto();
        userRepositoryDto.setRepositoryId("releases");
        userRepositoryDto.getRepositoryPrivileges().add( new PrivilegeDto("ARTIFACTS_RESOLVE","ARTIFACTS_RESOLVE"));

        UserStorageDto userStorageDto = new UserStorageDto();
        userStorageDto.setStorageId("storage0");
        userStorageDto.getRepositories().add(userRepositoryDto);

        userAccessModelDto.getStorages().add(userStorageDto);

        user.setUserAccessModel(userAccessModelDto);

        UserForm userForm = buildFromUser(new User(user), u -> u.setEnabled(true));

        // create new user
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
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
        assertEquals(username, createdUser.getUsername());

        // By default assignableRoles should not be present in the response.
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/{name}", username)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("user.username", equalTo(username))
               .body("user.accessModel.repositoriesAccess", notNullValue())
               .body("user.accessModel.repositoriesAccess", hasSize(greaterThan(0)))
               .body("assignableRoles", nullValue());


        // assignableRoles should be present only if there is ?assignableRoles=true in the request.
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/{name}?formFields=true", username)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("user.username", equalTo(username))
               .body("user.accessModel.repositoriesAccess", notNullValue())
               .body("user.accessModel.repositoriesAccess", hasSize(greaterThan(0)))
               .body("assignableRoles", notNullValue())
               .body("assignableRoles", hasSize(greaterThan(0)))
               .body("assignablePrivileges", notNullValue())
               .body("assignablePrivileges", hasSize(greaterThan(0)));

        deleteCreatedUser(username);
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

    @ParameterizedTest
    @MethodSource("usersProvider")
    void createUser(String acceptHeader,
                    String username)
    {
        deleteCreatedUser(username);
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
        deleteCreatedUser(username);
    }

    @ParameterizedTest
    @MethodSource("usersProvider")
    void creatingUserWithExistingUsernameShouldFail(String acceptHeader,
                                                    String username)
    {
        deleteCreatedUser(username);
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
        deleteCreatedUser(username);
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

    @ParameterizedTest
    @MethodSource("usersProvider")
    void updateUser(String acceptHeader,
                    String username)
    {
        deleteCreatedUser(username);
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
        deleteCreatedUser(username);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void updateExistingUserWithNullPassword(String acceptHeader)
    {
        User mavenUser = retrieveUserByName("deployer");
        UserForm input = buildFromUser(mavenUser, null);
        input.setPassword(null);

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

        User updatedUser = retrieveUserByName("deployer");

        assertNotNull(updatedUser.getPassword());
        assertEquals(mavenUser.getPassword(), updatedUser.getPassword());
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void createNewUserWithNullPassword(String acceptHeader)
    {
        UserDto newUserDto = new UserDto();
        newUserDto.setUsername("new-username-with-null-password");

        User newUser = new User(newUserDto);
        UserForm input = buildFromUser(newUser, null);
        input.setPassword(null);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
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

        assertNull(databaseCheck);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void setBlankPasswordExistingUser(String acceptHeader)
    {
        UserDto newUserDto = new UserDto();
        newUserDto.setUsername("new-username-with-blank-password");

        User newUser = new User(newUserDto);
        UserForm input = buildFromUser(newUser, null);
        input.setPassword("         ");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
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

        assertNull(databaseCheck);
    }

    @ParameterizedTest
    @WithUserDetails("admin")
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void changeOwnUser(String acceptHeader)
    {
        final String username = "admin";
        final String newPassword = "";
        UserForm user = buildUser(username, newPassword);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
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

        assertEquals(username, updatedUser.getUsername());
        assertFalse(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    @WithUserDetails("admin")
    void shouldBeAbleToUpdateRoles(String acceptHeader)
    {
        final String username = "test-user";
        final String newPassword = "password";

        UserDto user = new UserDto();
        user.setEnabled(true);
        user.setUsername(username);
        user.setPassword(newPassword);
        user.setSecurityTokenKey("some-security-token");
        user.setRoles(ImmutableSet.of(Roles.UI_MANAGER.name()));
        userService.save(user);

        UserForm admin = buildUser(username, newPassword);

        User updatedUser = retrieveUserByName(admin.getUsername());

        assertTrue(SetUtils.isEqualSet(updatedUser.getRoles(), ImmutableSet.of(Roles.UI_MANAGER.name())));

        admin.setRoles(ImmutableSet.of("ADMIN"));
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

        assertTrue(SetUtils.isEqualSet(updatedUser.getRoles(), ImmutableSet.of("ADMIN")));

        // Rollback changes.
        admin.setRoles(ImmutableSet.of(Roles.UI_MANAGER.name()));
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
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
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/admin")
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value());

        given().accept(MediaType.APPLICATION_JSON_VALUE)
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

        deleteCreatedUser(username);
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

        deleteCreatedUser(username);
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

    @Disabled // disabled temporarily
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

        RepositoryAccessModelForm form = new RepositoryAccessModelForm();
        form.setStorageId("storage0");
        form.setRepositoryId("releases");
        form.setPrivileges(Lists.newArrayList("ARTIFACTS_RESOLVE"));
        test.getAccessModel().addRepositoryAccess(form);

        form = new RepositoryAccessModelForm();
        form.setStorageId("storage0");
        form.setRepositoryId("releases");
        form.setWildcard(true);
        form.setPath("com/mycorp/");
        form.setPrivileges(Lists.newArrayList("ARTIFACTS_RESOLVE"));
        test.getAccessModel().addRepositoryAccess(form);

        form = new RepositoryAccessModelForm();
        form.setStorageId("storage0");
        form.setRepositoryId("releases");
        form.setPath("com/mycorp2/");
        form.setPrivileges(Lists.newArrayList("ARTIFACTS_RESOLVE"));
        test.getAccessModel().addRepositoryAccess(form);

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

        assertFalse(accessModel.getRepositoriesAccess().isEmpty());

        AccessModelForm accessModelForm = buildFromAccessModel(accessModel);

        // modify access model and save it
        final String mockPrivilege = Privileges.ARTIFACTS_DELETE.toString();

        form = new RepositoryAccessModelForm();
        form.setStorageId("storage0");
        form.setRepositoryId("act-releases-1");
        form.setPath("org/carlspring/strongbox");
        form.setPrivileges(Collections.singleton(mockPrivilege));
        accessModelForm.addRepositoryAccess(form);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(accessModelForm)
               .put(getContextBaseUrl() + "/{username}/access-model", username)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value());

        UserOutput updatedUser = getUser(username);

        AccessModelOutput updatedModel = updatedUser.getAccessModel();
        assertNotNull(updatedModel);

        logger.debug(updatedModel.toString());

        Optional<RepositoryAccessModelOutput> repositoryAccess = updatedModel.getRepositoriesAccess()
                                                                             .stream()
                                                                             .filter(a -> "org/carlspring/strongbox".equals(
                                                                                     a.getPath()))
                                                                             .findFirst();

        assertNotNull(repositoryAccess);
        assertTrue(repositoryAccess.isPresent());
        logger.info("$$$$$$$$$$$$$$$$$$$$$$$$$" + repositoryAccess.get().getPrivileges());
        assertTrue(repositoryAccess.get().getPrivileges().contains(mockPrivilege));
    }

    @ParameterizedTest
    @MethodSource("usersProvider")
    public void testNotValidMapsShouldNotUpdateAccessModel(String acceptHeader)
    {
        String username = "test-user";

        deleteCreatedUser(username);

        UserDto user = new UserDto();
        user.setEnabled(true);
        user.setUsername(username);
        user.setPassword("test-password");
        user.setSecurityTokenKey("before");

        UserAccessModelDto userAccessModelDto = new UserAccessModelDto();

        UserRepositoryDto userRepositoryDto = new UserRepositoryDto();
        userRepositoryDto.setRepositoryId("releases");
        userRepositoryDto.getRepositoryPrivileges().add(new PrivilegeDto("ARTIFACTS_RESOLVE","ARTIFACTS_RESOLVE"));

        UserStorageDto userStorageDto = new UserStorageDto();
        userStorageDto.setStorageId("storage0");

        userStorageDto.getRepositories().add(userRepositoryDto);

        userAccessModelDto.getStorages().add(userStorageDto);

        user.setUserAccessModel(userAccessModelDto);

        UserForm userForm = buildFromUser(new User(user), u -> u.setEnabled(true));

        // create new user
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
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
        assertEquals(username, createdUser.getUsername());

        // load user with custom access model
        UserOutput test = getUser(username);
        AccessModelForm accessModel = buildFromAccessModel(test.getAccessModel());

        assertNotNull(accessModel);

        logger.debug(accessModel.toString());

        assertFalse(accessModel.getRepositoriesAccess().isEmpty());

        // modify access model and save it

        RepositoryAccessModelForm form = new RepositoryAccessModelForm();
        form.setStorageId("storage0");
        form.setRepositoryId("act-releases-1");
        form.setPath("org/carlspring/strongbox");
        form.setPrivileges(Collections.emptyList());
        accessModel.addRepositoryAccess(form);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(accessModel)
               .put(getContextBaseUrl() + "/{username}/access-model", username)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_UPDATE_ACCESS_MODEL));

        deleteCreatedUser(username);
    }

    @ParameterizedTest
    @MethodSource("usersProvider")
    public void testUpdatingAccessModelForNonExistingUserShouldFail(String acceptHeader)
    {
        String testUsername = "test-user";

        deleteCreatedUser(testUsername);

        UserDto user = new UserDto();
        user.setEnabled(true);
        user.setUsername(testUsername);
        user.setPassword("test-password");
        user.setSecurityTokenKey("before");

        UserAccessModelDto userAccessModelDto = new UserAccessModelDto();

        UserRepositoryDto userRepositoryDto = new UserRepositoryDto();
        userRepositoryDto.setRepositoryId("releases");
        userRepositoryDto.getRepositoryPrivileges().add(new PrivilegeDto("ARTIFACTS_RESOLVE","ARTIFACTS_RESOLVE"));

        UserStorageDto userStorageDto = new UserStorageDto();
        userStorageDto.setStorageId("storage0");

        userStorageDto.getRepositories().add(userRepositoryDto);

        userAccessModelDto.getStorages().add(userStorageDto);

        user.setUserAccessModel(userAccessModelDto);

        UserForm userForm = buildFromUser(new User(user), u -> u.setEnabled(true));

        // create new user
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
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
        UserOutput test = getUser(testUsername);
        assertEquals(testUsername, test.getUsername());

        logger.debug(testUsername);

        AccessModelForm accessModel = buildFromAccessModel(test.getAccessModel());

        logger.debug(accessModel.toString());

        assertNotNull(accessModel);

        logger.debug(accessModel.toString());

        assertFalse(accessModel.getRepositoriesAccess().isEmpty());

        // modify access model and save it
        final String mockPrivilege = Privileges.ARTIFACTS_DELETE.toString();

        RepositoryAccessModelForm form = new RepositoryAccessModelForm();
        form.setStorageId("storage0");
        form.setRepositoryId("act-releases-1");
        form.setPath("org/carlspring/strongbox");
        form.setPrivileges(Collections.singletonList(mockPrivilege));
        accessModel.addRepositoryAccess(form);

        String username = "userNotFound";
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(accessModel)
               .put(getContextBaseUrl() + "/{username}/access-model", username)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value()) // check http status code
               .body(containsString(NOT_FOUND_USER));

        deleteCreatedUser(testUsername);
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

    private void deleteCreatedUser(String username)
    {
        logger.info("Delete created user: " + username);
        userService.delete(username);
    }

    // get user through REST API
    private UserOutput getUser(String username)
    {
        UserResponseEntity responseEntity = given().accept(MediaType.APPLICATION_JSON_VALUE)
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
        dto.setAccessModel(
                buildFromAccessModel(AccessModelToAccessModelOutputConverter.INSTANCE.convert(user.getUserAccessModel())));
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
            BeanUtils.copyProperties(accessModel, dto);
        }
        return dto;
    }

}
