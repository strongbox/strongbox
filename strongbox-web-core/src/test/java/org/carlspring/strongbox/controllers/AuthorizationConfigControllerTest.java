package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.security.Role;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class AuthorizationConfigControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    AuthorizationConfigProvider configProvider;

    @Test
    public void testThatRoleCouldBeAdded()
            throws Exception
    {
        // prepare new role
        final Role customRole = new Role();
        customRole.setName("MyNewRole".toUpperCase());
        customRole.setPrivileges(new HashSet<>(Arrays.asList(Privileges.ADMIN_LIST_REPO.name(),
                                                             Privileges.ARTIFACTS_DEPLOY.name())));

        RestAssuredMockMvc.given()
                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                          .body(customRole)
                          .when()
                          .post("/configuration/authorization/role")
                          .peek() // Use peek() to print the output
                          .then()
                          .statusCode(200); // check http status code
    }

    @Test
    public void testThatConfigXMLCouldBeDownloaded()
            throws Exception
    {
        RestAssuredMockMvc.given()
                          .contentType(APPLICATION_JSON_VALUE)
                          .when()
                          .get("/configuration/authorization/xml")
                          .peek() // Use peek() to print the output
                          .then()
                          .statusCode(200) // check http status code
                          .extract()
                          .statusCode();
    }

}