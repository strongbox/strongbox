package org.carlspring.strongbox.controllers.configuration.security.authorization;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.authorization.service.AuthorizationConfigService;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.PrivilegeForm;
import org.carlspring.strongbox.forms.PrivilegeListForm;
import org.carlspring.strongbox.forms.RoleForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.Privileges;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.controllers.configuration.security.authorization.AuthorizationConfigController.*;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@SpringBootTest
public class AuthorizationConfigControllerTestIT
        extends RestAssuredBaseTest
{

    @Inject
    private AuthorizationConfigService authorizationConfigService;

    private AuthorizationConfigDto config;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/api/configuration");
        config = authorizationConfigService.getDto();
    }

    @AfterEach
    public void afterEveryTest()
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
        customRole.setRepository("Test repository");
        customRole.setPrivileges(new HashSet<>(Arrays.asList(Privileges.ADMIN_LIST_REPO.name(),
                Privileges.ARTIFACTS_DEPLOY.name())));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(customRole)
               .when()
               .post(getContextBaseUrl() + "/authorization/role")
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

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(acceptHeader)
                .body(customRole)
                .when()
                .post(getContextBaseUrl() + "/authorization/role")
                .peek()
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(containsString(FAILED_ADD_ROLE));
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.APPLICATION_XML_VALUE })
    void configXMLCouldBeDownloaded(String acceptHeader)
    {
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .when()
               .get(getContextBaseUrl() + "/authorization/xml")
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
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .when()
               .delete(getContextBaseUrl() + "/authorization/role/" + roleName)
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
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .when()
               .delete(getContextBaseUrl() + "/authorization/role/" + roleName)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value()) // check http status code
               .body(containsString(FAILED_DELETE_ROLE));
    }

    @ParameterizedTest
    @MethodSource("privilegesProvider")
    void privilegesToAnonymousShouldBeAdded(String privilegeName,
                                            String acceptHeader)
    {
        // assign privileges to anonymous user
        PrivilegeListForm privilegeListForm = new PrivilegeListForm();
        List<PrivilegeForm> privilegeForms = new ArrayList<>();
        PrivilegeForm privilegeForm = new PrivilegeForm(privilegeName, "");
        privilegeForms.add(privilegeForm);
        privilegeListForm.setPrivileges(privilegeForms);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(privilegeListForm)
               .when()
               .post(getContextBaseUrl() + "/authorization/anonymous/privileges")
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
        List<PrivilegeForm> privilegeForms = new ArrayList<>();
        PrivilegeForm privilegeForm = new PrivilegeForm(StringUtils.EMPTY, "");
        privilegeForms.add(privilegeForm);
        privilegeListForm.setPrivileges(privilegeForms);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(privilegeListForm)
               .when()
               .post(getContextBaseUrl() + "/authorization/anonymous/privileges")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value()) // check http status code
               .body(containsString(FAILED_ASSIGN_PRIVILEGES));
    }

}
