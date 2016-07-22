package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.rest.context.RestletTestContext;
import org.carlspring.strongbox.security.jaas.Role;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;

/**
 * Simple test for {@link AuthorizationConfigRestlet}.
 *
 * @author Alex Oreshkevich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@RestletTestContext
public class AuthorizationConfigRestletTest
{

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationConfigRestletTest.class);

    private static RestClient client = new RestClient();
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

        Entity entity = Entity.entity(objectMapper.writeValueAsString(customRole), MediaType.TEXT_PLAIN);

        Response response = client.prepareTarget("/configuration/authorization/role").request().post(entity);
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test
    public synchronized void testThatConfigXMLCouldBeDownloaded()
            throws Exception
    {
        Response response = client.prepareTarget("/configuration/authorization/xml").request().get();
        assertEquals(HttpStatus.SC_OK, response.getStatus());
        logger.debug("\n" + response.readEntity(String.class));
    }
}
