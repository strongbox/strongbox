package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.users.domain.User;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link UserRestlet} REST API.
 */
public class UserRestletTest
        extends CustomJerseyTest
{

    private static final Logger logger = LoggerFactory.getLogger(UserRestletTest.class);

    @Test
    public void testSayHello()
    {
        Response response = requestApi("/users/greet").get();
        displayResponseError(response);
    }

    @Test
    public void testRetrieveUser()
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
        Response response = requestApi("/users/user/" + name).get();
        if (response.getStatus() != HttpStatus.SC_OK)
        {
            displayResponseError(response);
            throw new Error();
        }

        String rawResponse = read(response);

        User admin = objectMapper.readValue(rawResponse, User.class);
        return admin;
    }

    @Test
    public void testCreateUser()
            throws Exception
    {
        User test = buildUser("test", "password");

        Entity entity = Entity.entity(objectMapper.writeValueAsString(test), MediaType.TEXT_PLAIN);

        Response response = requestApi("/users/user").post(entity);
        assertTrue(response.getStatus() == HttpStatus.SC_OK);
    }

    @Test
    public void testRetrieveAllUsers()
            throws Exception
    {
        Response response = requestApi("/users/all").get();
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
    public void testUpdateUser()
            throws Exception
    {
        // create new user
        User test = buildUser("test-update", "password-update");
        Entity entity = Entity.entity(objectMapper.writeValueAsString(test), MediaType.TEXT_PLAIN);
        Response response = requestApi("/users/user").post(entity);
        assertTrue(response.getStatus() == HttpStatus.SC_OK);

        // retrieve newly created user and store the id
        User createdUser = retrieveUserByName(test.getUsername());
        assertNotNull("Created user should have id", createdUser.getId());

        // update some property for user
        createdUser.setEnabled(true);

        // send update request
        entity = Entity.entity(objectMapper.writeValueAsString(createdUser), MediaType.TEXT_PLAIN);
        Response updateResponse = requestApi("/users/user").put(entity);
        assertTrue(updateResponse.getStatus() == HttpStatus.SC_OK);

        // deserialize response
        String rawResponse = read(updateResponse);
        User updatedUser = objectMapper.readValue(rawResponse, User.class);

        assertEquals(createdUser, updatedUser);
    }

    @Test
    public void testDeleteUser()
            throws Exception
    {
        // create new user
        User test = buildUser("test-update", "password-update");
        Entity entity = Entity.entity(objectMapper.writeValueAsString(test), MediaType.TEXT_PLAIN);
        Response response = requestApi("/users/user").post(entity);
        assertTrue(response.getStatus() == HttpStatus.SC_OK);

        Response deleteResponse = requestApi("/users/user/" + test.getUsername()).delete();
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

    private String read(Response response)
    {
        String rawResponse = response.readEntity(String.class);
        logger.debug("Raw response " + rawResponse);
        return rawResponse;
    }
}
