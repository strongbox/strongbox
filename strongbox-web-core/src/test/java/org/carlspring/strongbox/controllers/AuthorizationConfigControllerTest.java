package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.security.Privilege;
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
import static org.carlspring.strongbox.controllers.AuthorizationConfigController.*;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class AuthorizationConfigControllerTest
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
                                   String roleName)
            throws Exception
    {
        // prepare new role
        final Role customRole = new Role();
        customRole.setName(roleName);
        customRole.setPrivileges(new HashSet<>(Arrays.asList(Privileges.ADMIN_LIST_REPO.name(),
                                                             Privileges.ARTIFACTS_DEPLOY.name())));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .body(customRole)
               .when()
               .post("/configuration/authorization/role")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_ADD_ROLE));
    }

    @Test
    public void testRoleShouldBeAddedWithResponseInJson()
            throws Exception
    {
        String roleName = "TEST_ROLE";
        roleShouldBeAdded(MediaType.APPLICATION_JSON_VALUE, roleName);
    }

    @Test
    public void testRoleShouldBeAddedWithResponseInText()
            throws Exception
    {
        String roleName = "TEST_ROLE";
        roleShouldBeAdded(MediaType.TEXT_PLAIN_VALUE, roleName);
    }

    private void roleShouldNotBeAdded(String acceptHeader,
                                      String roleName)
            throws Exception
    {
        // prepare new role
        final Role customRole = new Role();
        customRole.setName(roleName);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .body(customRole)
               .when()
               .post("/configuration/authorization/role")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value()) // check http status code
               .body(containsString(FAILED_ADD_ROLE));
    }

    @Test
    public void testRoleShouldNotBeAddedWithResponseInJson()
            throws Exception
    {
        String existingRoleName = originalRoles.iterator().next().getName();
        roleShouldNotBeAdded(MediaType.APPLICATION_JSON_VALUE, existingRoleName);
    }

    @Test
    public void testRoleShouldNotBeAddedWithResponseInText()
            throws Exception
    {
        String existingRoleName = originalRoles.iterator().next().getName();
        roleShouldNotBeAdded(MediaType.TEXT_PLAIN_VALUE, existingRoleName);
    }

    private void configXMLCouldBeDownloaded(String acceptHeader)
            throws Exception
    {
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .get("/configuration/authorization/xml")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()); // check http status code
    }

    @Test
    public void testThatConfigXMLCouldBeDownloadedWithResponseInJson()
            throws Exception
    {
        configXMLCouldBeDownloaded(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void testThatConfigXMLCouldBeDownloadedWithResponseInXml()
            throws Exception
    {
        configXMLCouldBeDownloaded(MediaType.APPLICATION_XML_VALUE);
    }

    private void roleShouldBeDeleted(String acceptHeader,
                                     String roleName)
            throws Exception
    {
        // delete role
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .delete("/configuration/authorization/role/" + roleName)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_DELETE_ROLE));
    }

    @Test
    public void testRoleShouldBeDeletedWithResponseInJson()
            throws Exception
    {
        String roleName = originalRoles.iterator().next().getName();
        roleShouldBeDeleted(MediaType.APPLICATION_JSON_VALUE, roleName);
    }

    @Test
    public void testRoleShouldBeDeletedWithResponseInText()
            throws Exception
    {
        String roleName = originalRoles.iterator().next().getName();
        roleShouldBeDeleted(MediaType.TEXT_PLAIN_VALUE, roleName);
    }

    private void roleShouldNotBeDeleted(String acceptHeader,
                                        String roleName)
            throws Exception
    {
        // delete role
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .delete("/configuration/authorization/role/" + roleName)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value()) // check http status code
               .body(containsString(FAILED_DELETE_ROLE));
    }

    @Test
    public void testRoleShouldNotBeDeletedWithResponseInJson()
            throws Exception
    {
        String nonExistingRoleName = "TEST_ROLE";
        roleShouldNotBeDeleted(MediaType.APPLICATION_JSON_VALUE, nonExistingRoleName);
    }

    @Test
    public void testRoleShouldNotBeDeletedWithResponseInText()
            throws Exception
    {
        String nonExistingRoleName = "TEST_ROLE";
        roleShouldNotBeDeleted(MediaType.TEXT_PLAIN_VALUE, nonExistingRoleName);
    }

    private void privilegesToAnonymousShouldBeAdded(String acceptHeader,
                                                    String privilegeName)
            throws Exception
    {
        // assign privileges to anonymous user
        List<Privilege> privileges = new ArrayList<>();
        Privilege privilege = new Privilege(privilegeName, "");
        privileges.add(privilege);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .body(privileges)
               .when()
               .post("/configuration/authorization/anonymous/privileges")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_ASSIGN_PRIVILEGES));
    }

    @Test
    public void testPrivilegesToAnonymousShouldBeAddedWithResponseInJson()
            throws Exception
    {
        String privilegeName = Privileges.ADMIN_LIST_REPO.name();
        privilegesToAnonymousShouldBeAdded(MediaType.APPLICATION_JSON_VALUE, privilegeName);
    }

    @Test
    public void testPrivilegesToAnonymousShouldBeAddedWithResponseInText()
            throws Exception
    {
        String privilegeName = Privileges.ARTIFACTS_DEPLOY.name();
        privilegesToAnonymousShouldBeAdded(MediaType.TEXT_PLAIN_VALUE, privilegeName);
    }

    private void rolesToAnonymousShouldBeAdded(String acceptHeader,
                                               String roleName)
            throws Exception
    {
        // assign roles to anonymous user
        List<Role> roles = new ArrayList<>();
        final Role customRole = new Role();
        customRole.setName(roleName);
        customRole.setPrivileges(new HashSet<>(Arrays.asList(Privileges.ADMIN_LIST_REPO.name(),
                                                             Privileges.ARTIFACTS_DEPLOY.name())));
        roles.add(customRole);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .body(roles)
               .when()
               .post("/configuration/authorization/anonymous/roles")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_ASSIGN_ROLES));
    }

    @Test
    public void testRolesToAnonymousShouldBeAddedWithResponseInJson()
            throws Exception
    {
        String roleName = "TEST_ROLE";
        rolesToAnonymousShouldBeAdded(MediaType.APPLICATION_JSON_VALUE, roleName);
    }

    @Test
    public void testRolesToAnonymousShouldBeAddedWithResponseInText()
            throws Exception
    {
        String roleName = "TEST_ROLE";
        rolesToAnonymousShouldBeAdded(MediaType.TEXT_PLAIN_VALUE, roleName);
    }
}
