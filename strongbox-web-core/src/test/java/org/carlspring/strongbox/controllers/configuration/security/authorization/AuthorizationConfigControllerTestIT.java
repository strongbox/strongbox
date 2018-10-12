package org.carlspring.strongbox.controllers.configuration.security.authorization;

import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.authorization.service.AuthorizationConfigService;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.PrivilegeForm;
import org.carlspring.strongbox.forms.PrivilegeListForm;
import org.carlspring.strongbox.forms.RoleForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.Privileges;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.controllers.configuration.security.authorization.AuthorizationConfigController.*;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class AuthorizationConfigControllerTestIT
        extends RestAssuredBaseTest
{

    @Inject
    private AuthorizationConfigService authorizationConfigService;

    private AuthorizationConfigDto config;

    @BeforeEach
    public void beforeEveryTest()
    {
        config = authorizationConfigService.getDto();
    }

    @AfterEach
    public void afterEveryTest()
    {
        authorizationConfigService.setAuthorizationConfig(config);
    }

    private void roleShouldBeAdded(String acceptHeader,
                                   RoleForm role)
    {
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .body(role)
               .when()
               .post("/api/configuration/authorization/role")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_ADD_ROLE));
    }

    @Test
    public void testRoleShouldBeAddedWithResponseInJson()
    {
        final RoleForm customRole = new RoleForm();
        customRole.setName("TEST_ROLE");
        customRole.setDescription("Test role");
        customRole.setRepository("Test repository");
        customRole.setPrivileges(new HashSet<>(Arrays.asList(Privileges.ADMIN_LIST_REPO.name(),
                                                             Privileges.ARTIFACTS_DEPLOY.name())));
        roleShouldBeAdded(MediaType.APPLICATION_JSON_VALUE, customRole);
    }

    @Test
    public void testRoleShouldBeAddedWithResponseInText()
    {
        final RoleForm customRole = new RoleForm();
        customRole.setName("TEST_ROLE");
        customRole.setDescription("Test role");
        customRole.setRepository("Test repository");
        customRole.setPrivileges(new HashSet<>(Arrays.asList(Privileges.ADMIN_LIST_REPO.name(),
                                                             Privileges.ARTIFACTS_DEPLOY.name())));
        roleShouldBeAdded(MediaType.TEXT_PLAIN_VALUE, customRole);
    }

    private void roleShouldNotBeAdded(String acceptHeader,
                                      String roleName)
    {
        // prepare new role
        final RoleDto customRole = new RoleDto();
        customRole.setName(roleName);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .body(customRole)
               .when()
               .post("/api/configuration/authorization/role")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value()) // check http status code
               .body(containsString(FAILED_ADD_ROLE));
    }

    @Test
    public void testExistingRoleShouldNotBeAddedWithResponseInJson()
    {
        String existingRoleName = config.getRoles().iterator().next().getName();
        roleShouldNotBeAdded(MediaType.APPLICATION_JSON_VALUE, existingRoleName);
    }

    @Test
    public void testExistingRoleShouldNotBeAddedWithResponseInText()
    {
        String existingRoleName = config.getRoles().iterator().next().getName();
        roleShouldNotBeAdded(MediaType.TEXT_PLAIN_VALUE, existingRoleName);
    }

    @Test
    public void testEmptyRoleNameShouldNotBeAddedWithResponseInJson()
    {
        String roleName = "";
        roleShouldNotBeAdded(MediaType.APPLICATION_JSON_VALUE, roleName);
    }

    @Test
    public void testEmptyRoleNameShouldNotBeAddedWithResponseInText()
    {
        String roleName = "";
        roleShouldNotBeAdded(MediaType.TEXT_PLAIN_VALUE, roleName);
    }

    private void configXMLCouldBeDownloaded(String acceptHeader)
    {
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .get("/api/configuration/authorization/xml")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()); // check http status code
    }

    @Test
    public void testThatConfigXMLCouldBeDownloadedWithResponseInJson()
    {
        configXMLCouldBeDownloaded(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void testThatConfigXMLCouldBeDownloadedWithResponseInXml()
    {
        configXMLCouldBeDownloaded(MediaType.APPLICATION_XML_VALUE);
    }

    private void roleShouldBeDeleted(String acceptHeader,
                                     String roleName)
    {
        // delete role
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .delete("/api/configuration/authorization/role/" + roleName)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_DELETE_ROLE));
    }

    @Test
    public void testRoleShouldBeDeletedWithResponseInJson()
    {
        String roleName = config.getRoles().iterator().next().getName();
        roleShouldBeDeleted(MediaType.APPLICATION_JSON_VALUE, roleName);
    }

    @Test
    public void testRoleShouldBeDeletedWithResponseInText()
    {
        String roleName = config.getRoles().iterator().next().getName();
        roleShouldBeDeleted(MediaType.TEXT_PLAIN_VALUE, roleName);
    }

    private void roleShouldNotBeDeleted(String acceptHeader,
                                        String roleName)
    {
        // delete role
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .delete("/api/configuration/authorization/role/" + roleName)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value()) // check http status code
               .body(containsString(FAILED_DELETE_ROLE));
    }

    @Test
    public void testRoleShouldNotBeDeletedWithResponseInJson()
    {
        String nonExistingRoleName = "TEST_ROLE";
        roleShouldNotBeDeleted(MediaType.APPLICATION_JSON_VALUE, nonExistingRoleName);
    }

    @Test
    public void testRoleShouldNotBeDeletedWithResponseInText()
    {
        String nonExistingRoleName = "TEST_ROLE";
        roleShouldNotBeDeleted(MediaType.TEXT_PLAIN_VALUE, nonExistingRoleName);
    }

    private void privilegesToAnonymousShouldBeAdded(String acceptHeader,
                                                    String privilegeName)
    {
        // assign privileges to anonymous user
        PrivilegeListForm privilegeListForm = new PrivilegeListForm();
        List<PrivilegeForm> privilegeForms = new ArrayList<>();
        PrivilegeForm privilegeForm = new PrivilegeForm(privilegeName, "");
        privilegeForms.add(privilegeForm);
        privilegeListForm.setPrivileges(privilegeForms);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .body(privilegeListForm)
               .when()
               .post("/api/configuration/authorization/anonymous/privileges")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_ASSIGN_PRIVILEGES));
    }

    @Test
    public void testPrivilegesToAnonymousShouldBeAddedWithResponseInJson()
    {
        String privilegeName = Privileges.ADMIN_LIST_REPO.name();
        privilegesToAnonymousShouldBeAdded(MediaType.APPLICATION_JSON_VALUE, privilegeName);
    }

    @Test
    public void testPrivilegesToAnonymousShouldBeAddedWithResponseInText()
    {
        String privilegeName = Privileges.ARTIFACTS_DEPLOY.name();
        privilegesToAnonymousShouldBeAdded(MediaType.TEXT_PLAIN_VALUE, privilegeName);
    }

    private void privilegesToAnonymousShouldNotBeAdded(String acceptHeader,
                                                       String privilegeName)
    {
        // assign privileges to anonymous user
        PrivilegeListForm privilegeListForm = new PrivilegeListForm();
        List<PrivilegeForm> privilegeForms = new ArrayList<>();
        PrivilegeForm privilegeForm = new PrivilegeForm(privilegeName, "");
        privilegeForms.add(privilegeForm);
        privilegeListForm.setPrivileges(privilegeForms);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .body(privilegeListForm)
               .when()
               .post("/api/configuration/authorization/anonymous/privileges")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value()) // check http status code
               .body(containsString(FAILED_ASSIGN_PRIVILEGES));
    }

    @Test
    public void testEmptyPrivilegeNameToAnonymousShouldNotBeAddedWithResponseInJson()
    {
        String privilegeName = "";
        privilegesToAnonymousShouldNotBeAdded(MediaType.APPLICATION_JSON_VALUE, privilegeName);
    }

    @Test
    public void testEmptyPrivilegeNameToAnonymousShouldNotBeAddedWithResponseInText()
    {
        String privilegeName = "";
        privilegesToAnonymousShouldNotBeAdded(MediaType.TEXT_PLAIN_VALUE, privilegeName);
    }

}
