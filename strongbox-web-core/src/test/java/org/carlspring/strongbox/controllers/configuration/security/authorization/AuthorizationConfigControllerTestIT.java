package org.carlspring.strongbox.controllers.configuration.security.authorization;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.PrivilegeForm;
import org.carlspring.strongbox.forms.PrivilegeListForm;
import org.carlspring.strongbox.forms.RoleForm;
import org.carlspring.strongbox.forms.RoleListForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.security.Role;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.security.AuthorizationConfig;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;

import javax.inject.Inject;
import java.util.*;

import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.controllers.configuration.security.authorization.AuthorizationConfigController.*;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class AuthorizationConfigControllerTestIT
        extends RestAssuredBaseTest
{

    @Inject
    private AuthorizationConfigProvider configProvider;

    private Set<Role> originalRoles;

    @Before
    public void beforeEveryTest()
    {
        Optional<AuthorizationConfig> configOptional = configProvider.getConfig();
        // Saves original roles list.
        configOptional.ifPresent(authorizationConfig ->
                                         originalRoles = Sets.newHashSet(authorizationConfig.getRoles().getRoles()));
        configOptional.orElseThrow(() -> new RuntimeException("Unable to load config"));
    }

    @After
    public void afterEveryTest()
    {
        Optional<AuthorizationConfig> configOptional = configProvider.getConfig();
        // Retrieve original roles list and updates the config.
        configOptional.ifPresent(authorizationConfig ->
                                 {
                                     authorizationConfig.getRoles().setRoles(originalRoles);
                                     configProvider.updateConfig(authorizationConfig);
                                 }
        );
        configOptional.orElseThrow(() -> new RuntimeException("Unable to load config"));
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
        final Role customRole = new Role();
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
        String existingRoleName = originalRoles.iterator().next().getName();
        roleShouldNotBeAdded(MediaType.APPLICATION_JSON_VALUE, existingRoleName);
    }

    @Test
    public void testExistingRoleShouldNotBeAddedWithResponseInText()
    {
        String existingRoleName = originalRoles.iterator().next().getName();
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
        String roleName = originalRoles.iterator().next().getName();
        roleShouldBeDeleted(MediaType.APPLICATION_JSON_VALUE, roleName);
    }

    @Test
    public void testRoleShouldBeDeletedWithResponseInText()
    {
        String roleName = originalRoles.iterator().next().getName();
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

    private void rolesToAnonymousShouldBeAdded(String acceptHeader,
                                               String roleName)
    {
        // assign roles to anonymous user
        RoleListForm roleListForm = new RoleListForm();
        List<RoleForm> roleForms = new ArrayList<>();
        final RoleForm roleForm = new RoleForm();
        roleForm.setName(roleName);
        roleForm.setPrivileges(new HashSet<>(Arrays.asList(Privileges.ADMIN_LIST_REPO.name(),
                                                           Privileges.ARTIFACTS_DEPLOY.name())));
        roleForms.add(roleForm);
        roleListForm.setRoles(roleForms);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .body(roleListForm)
               .when()
               .post("/api/configuration/authorization/anonymous/roles")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_ASSIGN_ROLES));
    }

    @Test
    public void testRolesToAnonymousShouldBeAddedWithResponseInJson()
    {
        String roleName = "TEST_ROLE";
        rolesToAnonymousShouldBeAdded(MediaType.APPLICATION_JSON_VALUE, roleName);
    }

    @Test
    public void testRolesToAnonymousShouldBeAddedWithResponseInText()
    {
        String roleName = "TEST_ROLE";
        rolesToAnonymousShouldBeAdded(MediaType.TEXT_PLAIN_VALUE, roleName);
    }

    private void rolesToAnonymousShouldNotBeAdded(String acceptHeader,
                                                  String roleName)
    {
        // assign roles to anonymous user
        RoleListForm roleListForm = new RoleListForm();
        List<RoleForm> roleForms = new ArrayList<>();
        final RoleForm roleForm = new RoleForm();
        roleForm.setName(roleName);
        roleForm.setPrivileges(new HashSet<>(Arrays.asList(Privileges.ADMIN_LIST_REPO.name(),
                                                           Privileges.ARTIFACTS_DEPLOY.name())));
        roleForms.add(roleForm);
        roleListForm.setRoles(roleForms);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .body(roleListForm)
               .when()
               .post("/api/configuration/authorization/anonymous/roles")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value()) // check http status code
               .body(containsString(FAILED_ASSIGN_ROLES));
    }

    @Test
    public void testEmptyRoleNameToAnonymousShouldNotBeAddedWithResponseInJson()
    {
        String roleName = "";
        rolesToAnonymousShouldNotBeAdded(MediaType.APPLICATION_JSON_VALUE, roleName);
    }

    @Test
    public void testEmptyRoleNameToAnonymousShouldNotBeAddedWithResponseInText()
    {
        String roleName = "";
        rolesToAnonymousShouldNotBeAdded(MediaType.TEXT_PLAIN_VALUE, roleName);
    }

}
