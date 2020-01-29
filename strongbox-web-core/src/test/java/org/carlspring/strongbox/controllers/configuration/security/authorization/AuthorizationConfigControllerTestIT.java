package org.carlspring.strongbox.controllers.configuration.security.authorization;

import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.authorization.service.AuthorizationConfigService;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.PrivilegeListForm;
import org.carlspring.strongbox.forms.RoleForm;
import org.carlspring.strongbox.forms.users.AccessModelForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.Privileges;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.carlspring.strongbox.controllers.configuration.security.authorization.AuthorizationConfigController.FAILED_ADD_ROLE;
import static org.carlspring.strongbox.controllers.configuration.security.authorization.AuthorizationConfigController.FAILED_ASSIGN_PRIVILEGES;
import static org.carlspring.strongbox.controllers.configuration.security.authorization.AuthorizationConfigController.FAILED_DELETE_ROLE;
import static org.carlspring.strongbox.controllers.configuration.security.authorization.AuthorizationConfigController.SUCCESSFUL_ADD_ROLE;
import static org.carlspring.strongbox.controllers.configuration.security.authorization.AuthorizationConfigController.SUCCESSFUL_ASSIGN_PRIVILEGES;
import static org.carlspring.strongbox.controllers.configuration.security.authorization.AuthorizationConfigController.SUCCESSFUL_DELETE_ROLE;
import static org.carlspring.strongbox.net.MediaType.APPLICATION_YAML_VALUE;
import static org.hamcrest.CoreMatchers.containsString;


/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class AuthorizationConfigControllerTestIT
        extends RestAssuredBaseTest
{

    @Inject
    private AuthorizationConfigService authorizationConfigService;

    private AuthorizationConfigDto config;

    private static final String EXISTING_ROLE = "CUSTOM_ROLE";

    private static Stream<Arguments> privilegesProvider()
    {
        return Stream.of(
                Arguments.of(Privileges.ADMIN_LIST_REPO, MediaType.APPLICATION_JSON_VALUE),
                Arguments.of(Privileges.ARTIFACTS_DEPLOY, MediaType.TEXT_PLAIN_VALUE)
        );
    }

    private static Stream<Arguments> rolesProvider()
    {
        return Stream.of(
                Arguments.of(EXISTING_ROLE, MediaType.APPLICATION_JSON_VALUE),
                Arguments.of(EXISTING_ROLE, MediaType.TEXT_PLAIN_VALUE),
                Arguments.of(StringUtils.EMPTY, MediaType.APPLICATION_JSON_VALUE),
                Arguments.of(StringUtils.EMPTY, MediaType.TEXT_PLAIN_VALUE)
        );
    }

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl("/api/configuration/authorization");
        config = authorizationConfigService.getDto();
    }

    @AfterEach
    public void afterEveryTest() throws IOException
    {
        authorizationConfigService.setAuthorizationConfig(config);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void roleShouldBeAdded(String acceptHeader)
    {
        final RoleForm customRole = new RoleForm();
        customRole.setName("TEST_ROLE");
        customRole.setDescription("Test role");
        AccessModelForm accessModel = new AccessModelForm();
        accessModel.setApiAccess(Arrays.asList(Privileges.ADMIN_LIST_REPO.name(),
                                              Privileges.ARTIFACTS_DEPLOY.name()));
        customRole.setAccessModel(accessModel);

        String url = getContextBaseUrl() + "/role";
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(customRole)
               .when()
               .post(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_ADD_ROLE));
    }

    @ParameterizedTest
    @MethodSource("rolesProvider")
    void roleShouldNotBeAdded(String roleName,
                              String acceptHeader)
    {
        final RoleDto customRole = new RoleDto();
        customRole.setName(roleName);

        String url = getContextBaseUrl() + "/role";
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(customRole)
               .when()
               .post(url)
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_ADD_ROLE));
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             APPLICATION_YAML_VALUE })
    void configFileCouldBeDownloaded(String acceptHeader)
    {
        String url = getContextBaseUrl();
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .when()
               .get(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()); // check http status code
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void roleShouldBeDeleted(String acceptHeader)
    {
        // get role name
        String roleName = config.getRoles().iterator().next().getName();

        // delete role
        String url = getContextBaseUrl() + "/role/{name}";
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .when()
               .delete(url, roleName)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_DELETE_ROLE));
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void roleShouldNotBeDeleted(String acceptHeader)
    {
        // init not existing role name
        String roleName = "TEST_ROLE";

        // delete role
        String url = getContextBaseUrl() + "/role/{name}";
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .when()
               .delete(url, roleName)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value()) // check http status code
               .body(containsString(FAILED_DELETE_ROLE));
    }

    @ParameterizedTest
    @MethodSource("privilegesProvider")
    void privilegesToAnonymousShouldBeAdded(Privileges privilege,
                                            String acceptHeader)
    {
        // assign privileges to anonymous user
        PrivilegeListForm privilegeListForm = new PrivilegeListForm();
        privilegeListForm.setPrivileges(Collections.singletonList(privilege));

        String url = getContextBaseUrl() + "/anonymous/privileges";
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(privilegeListForm)
               .when()
               .post(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_ASSIGN_PRIVILEGES));
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void emptyPrivilegesNameToAnonymousShouldNotBeAdded(String acceptHeader)
    {
        // assign privileges to anonymous user
        PrivilegeListForm privilegeListForm = new PrivilegeListForm();
        privilegeListForm.setPrivileges(Collections.emptyList());

        String url = getContextBaseUrl() + "/anonymous/privileges";
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(privilegeListForm)
               .when()
               .post(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value()) // check http status code
               .body(containsString(FAILED_ASSIGN_PRIVILEGES));
    }

}
