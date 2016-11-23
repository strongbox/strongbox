package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.security.Role;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.security.AuthorizationConfig;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.util.Arrays;
import java.util.HashSet;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
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

    @Inject
    OObjectDatabaseTx databaseTx;

    @Test
    public void testThatRoleCouldBeAdded()
            throws Exception
    {
        // prepare new role
        final Role customRole = new Role();
        customRole.setName("MyNewRole".toUpperCase());
        customRole.setPrivileges(new HashSet<>(Arrays.asList(Privileges.ADMIN_LIST_REPO.name(),
                                                             Privileges.ARTIFACTS_DEPLOY.name())));

        GenericParser<Role> parser = new GenericParser<>(Role.class);
        String serializedRole = parser.serialize(customRole);

        RestAssuredMockMvc.given()
                          .contentType(MediaType.TEXT_PLAIN_VALUE)
                          .body(serializedRole)
                          .when()
                          .post("/configuration/authorization/role")
                          .peek() // Use peek() to print the output
                          .then()
                          .statusCode(200); // check http status code
    }

    @Test
    public void testThatConfigCouldBeSerialized()
            throws Exception
    {
        final GenericParser<AuthorizationConfig> configGenericParser = new GenericParser<>(AuthorizationConfig.class);
        configProvider.getConfig().ifPresent(
                authorizationConfig ->
                {
                    try
                    {
                        logger.debug(configGenericParser.serialize(authorizationConfig));
                    }
                    catch (JAXBException e)
                    {
                        logger.error(e.getMessage(), e);
                    }
                });
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