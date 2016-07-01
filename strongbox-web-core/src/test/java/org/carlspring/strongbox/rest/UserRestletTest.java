package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.rest.context.RestletTestContext;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.carlspring.strongbox.users.domain.User;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@RestletTestContext
public class UserRestletTest
        extends TestCaseWithArtifactGeneration
{

    private static final Logger logger = LoggerFactory.getLogger(UserRestletTest.class);

    private static RestClient client = new RestClient();

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
    public synchronized void testSayHello()
    {
        Response response = client.prepareTarget("/users/greet").request().get();
        RestClient.displayResponseError(response);
    }

    @Test
    public synchronized void testRetrieveUser()
            throws Exception
    {
        final String userName = "admin";
        User admin = retrieveUserByName(userName);

        assertNotNull(admin);
        assertEquals(admin.getUsername(), userName);
    }

    private User retrieveUserByName(String name)
            throws IOException
    {
        Response response = client.prepareTarget("/users/user/" + name).request().get();
        if (response.getStatus() != HttpStatus.SC_OK)
        {
            RestClient.displayResponseError(response);
            throw new Error();
        }

        String rawResponse = read(response);

        User admin = objectMapper.readValue(rawResponse, User.class);
        return admin;
    }

    @Test
    public synchronized void testCreateUser()
            throws Exception
    {
        User test = buildUser("test", "password");

        Entity entity = Entity.entity(objectMapper.writeValueAsString(test), MediaType.TEXT_PLAIN);

        Response response = client.prepareTarget("/users/user").request(
                MediaType.TEXT_PLAIN).post(entity);
        assertTrue(response.getStatus() == HttpStatus.SC_OK);
    }

    @Test
    public synchronized void testRetrieveAllUsers()
            throws Exception
    {

        Response response = client.prepareTarget("/users/all").request().get();
        assertEquals(HttpStatus.SC_OK, response.getStatus());

        String rawResponse = read(response);

        List<User> users = objectMapper.readValue(rawResponse,
                                                  objectMapper.getTypeFactory().constructCollectionType(List.class,
                                                                                                        User.class));
        assertNotNull(users);
        assertFalse(users.isEmpty());

        users.forEach(user -> logger.debug("Retrieved " + user));
    }

    @Test
    public synchronized void testUpdateUser()
            throws Exception
    {
        // create new user
        User test = buildUser("test-update", "password-update");
        Entity entity = Entity.entity(objectMapper.writeValueAsString(test), MediaType.TEXT_PLAIN);
        Response response = client.prepareTarget("/users/user").request(
                MediaType.TEXT_PLAIN).post(entity);
        assertTrue(response.getStatus() == HttpStatus.SC_OK);

        // retrieve newly created user and store the id
        User createdUser = retrieveUserByName(test.getUsername());
        assertNotNull("Created user should have id", createdUser.getId());

        // update some property for user
        createdUser.setEnabled(true);

        // send update request
        entity = Entity.entity(objectMapper.writeValueAsString(createdUser), MediaType.TEXT_PLAIN);
        Response updateResponse = client.prepareTarget("/users/user").request(
                MediaType.TEXT_PLAIN).put(entity);
        assertTrue(updateResponse.getStatus() == HttpStatus.SC_OK);

        // deserialize response
        String rawResponse = read(updateResponse);
        User updatedUser = objectMapper.readValue(rawResponse, User.class);

        assertEquals(createdUser, updatedUser);
    }

    @Test
    public synchronized void testDeleteUser()
            throws Exception
    {
        // create new user
        User test = buildUser("test-update", "password-update");
        Entity entity = Entity.entity(objectMapper.writeValueAsString(test), MediaType.TEXT_PLAIN);
        Response response = client.prepareTarget("/users/user").request(
                MediaType.TEXT_PLAIN).post(entity);
        assertTrue(response.getStatus() == HttpStatus.SC_OK);

        Response deleteResponse = client.prepareTarget("/users/user/" + test.getUsername()).request().delete();
        assertTrue(deleteResponse.getStatus() == HttpStatus.SC_OK);
    }

    private User buildUser(String name,
                           String password)
    {
        User test = new User();
        test.setUsername(name);
        test.setPassword(password);
        test.setEnabled(false);
        return test;
    }

    private synchronized String read(Response response)
    {
        String rawResponse = response.readEntity(String.class);
        logger.debug("Raw response " + rawResponse);
        return rawResponse;
    }
}
