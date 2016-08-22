package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.security.jaas.Role;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.security.AuthorizationConfig;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.xml.bind.JAXBException;
import java.util.Arrays;
import java.util.HashSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by yury on 8/16/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
@WithUserDetails("admin")
public class AuthorizationConfigControllerTest
        extends BackendBaseTest
{

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationConfigRestletTest.class);

    private static RestClient client = new RestClient();
    private final GenericParser<AuthorizationConfig> configGenericParser = new GenericParser<>(AuthorizationConfig.class);

    @Autowired
    AuthorizationConfigProvider configProvider;

    @Autowired
    OObjectDatabaseTx databaseTx;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterClass
    public static void tearDown()
            throws Exception
    {
        if (client != null)
        {
            client.close();
        }
    }

    @Test
    public synchronized void testThatRoleCouldBeAdded()
            throws Exception
    {
        // prepare new role
        final Role customRole = new Role();
        customRole.setName("MyNewRole".toUpperCase());
        customRole.setPrivileges(
                new HashSet<>(Arrays.asList(Privileges.ADMIN_LIST_REPO.name(), Privileges.ARTIFACTS_DEPLOY.name())));

        //    Entity entity = Entity.entity(objectMapper.writeValueAsString(customRole), MediaType.TEXT_PLAIN);

        //  Response response = client.prepareTarget("/configuration/authorization/role").request().post(entity);

        GenericParser<Role> parser = new GenericParser<>(Role.class);
        String serializedRole = parser.serialize(customRole);

        RestAssuredMockMvc.given()
                          .contentType(MediaType.TEXT_PLAIN_VALUE)
                          .body(serializedRole)
                          .when()
                          .post("/configuration/authorization/role")
                          .peek() // Use peek() to print the ouput
                          .then()
                          .statusCode(200); // check http status code


        //  assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test
    public void testThatConfigCouldBeSerialized()
            throws Exception
    {
        configProvider.getConfig().ifPresent(authorizationConfig ->
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
    public synchronized void testThatConfigXMLCouldBeDownloaded()
            throws Exception
    {
        // Response response = client.prepareTarget("/configuration/authorization/xml").request().get();
        RestAssuredMockMvc.given()
                          .contentType(ContentType.JSON)
                          .when()
                          .get("/configuration/authorization/xml")
                          .peek() // Use peek() to print the ouput
                          .then()
                          .statusCode(200) // check http status code
                          .extract()
                          .statusCode();

      /*  if (response.getStatus() != HttpStatus.SC_OK)
        {
            RestClient.displayResponseError(response);
            throw new Exception(response.getStatusInfo().getReasonPhrase());
        }
        logger.debug("\n" + response.readEntity(String.class));*/
    }

}
