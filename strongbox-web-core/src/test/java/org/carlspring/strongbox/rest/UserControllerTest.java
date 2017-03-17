package org.carlspring.strongbox.rest;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.restassured.http.ContentType;

@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class UserControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    UserService userService;

    @Test
    public void greetTest()
    {
        displayAllUsers();

        given().contentType(ContentType.JSON)
               .param("name", "Johan")
               .when()
               .get("/users/greet")
               .then()
               .statusCode(200)
               .body(containsString("hello, Johan"));
    }

    @Test
    public void testRetrieveUser()
            throws Exception
    {
        final String userName = "admin";

        given().contentType(ContentType.JSON)
               .param("The name of the user", userName)
               .when()
               .get("/users/user/" + userName)
               .then()
               .statusCode(200)
               .body(containsString("admin"));
    }

    @Test
    public void testCreateUser()
            throws Exception
    {
        User test = buildUser("test", "password");

        given().contentType("application/json")
               .param("juser", test)
               .when()
               .post("/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(200) // check http status code
               .extract()
               .asString();

        displayAllUsers();
    }

    @Test
    public void testRetrieveAllUsers()
            throws Exception
    {

        String response = given().contentType("application/json")
                                 .when()
                                 .get("/users/all")
                                 .peek() // Use peek() to print the output
                                 .then()
                                 .statusCode(200) // check http status code
                                 .extract()
                                 .asString();

        List<User> users = objectMapper.readValue(response,
                                                  objectMapper.getTypeFactory()
                                                              .constructCollectionType(List.class,
                                                                                       User.class));

        assertNotNull(users);
        assertFalse(users.isEmpty());

        users.forEach(user -> logger.debug("Retrieved " + user));
    }

    @Test
    // com.orientechnologies.orient.core.storage.ORecordDuplicatedException
    public void testUpdateUser()
            throws Exception
    {
        // create new user
        final String userName = "test-update";
        User test = buildUser(userName, "password-update");

        given().contentType("application/json")
               .param("juser", test)
               .when()
               .post("/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(200) // check http status code
               .extract()
               .asString();

        // retrieve newly created user and store the objectId
        User createdUser = retrieveUserByName(test.getUsername());
        assertNotNull("Created user should have objectId", createdUser.getObjectId());
        assertEquals(userName, createdUser.getUsername());

        // update some property for user
        createdUser.setEnabled(true);

        logger.info("Users before update: ->>>>>> ");
        displayAllUsers();

        // send update request
        String response = given().contentType("application/json")
                                 .param("juser", createdUser)
                                 .when()
                                 .put("/users/user")
                                 .peek() // Use peek() to print the output
                                 .then()
                                 .statusCode(200) // check http status code
                                 .extract()
                                 .asString();

        // deserialize response
        User updatedUser = objectMapper.readValue(response, User.class);

        logger.info("Users after update: ->>>>>> ");
        displayAllUsers();

        assertEquals(userName, updatedUser.getUsername());
        assertEquals(createdUser.isEnabled(), updatedUser.isEnabled());
        assertEquals(createdUser.getPassword(), updatedUser.getPassword());
    }

    private void displayAllUsers()
    {

        // display all current users
        logger.info("All current users:");
        userService.findAll()
                   .ifPresent(users ->
                              {
                                  users.forEach(user -> logger.info(user.toString()));
                              });
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    // com.orientechnologies.orient.core.storage.ORecordDuplicatedException
    public void testGenerateSecurityToken()
            throws Exception
    {
        User user = buildUser("test-jwt", "password-update");

        //1. Create user
        given().contentType("application/json")
               .param("juser", user)
               .when()
               .post("/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(200) // check http status code
               .extract()
               .asString();

        user = retrieveUserByName(user.getUsername());
        assertNotNull("Created user should have objectId", user.getObjectId());

        //2. Provide `securityTokenKey`
        user.setSecurityTokenKey("seecret");
        String response = given().contentType("application/json")
                                 .param("juser", user)
                                 .when()
                                 .put("/users/user")
                                 .peek() // Use peek() to print the output
                                 .then()
                                 .statusCode(200) // check http status code
                                 .extract()
                                 .asString();
        user = objectMapper.readValue(response, User.class);
        assertNotNull(user.getSecurityTokenKey());

        //3. Generate token
        response = given().when()
                          .get("/users/user/test-jwt/generate-security-token")
                          .peek() // Use peek() to print the output
                          .then()
                          .statusCode(200) // check http status code
                          .extract()
                          .asString();
        assertTrue(response.startsWith("eyJhbGciOiJIUzI1NiJ9"));
    }

    @Test
    // com.orientechnologies.orient.core.storage.ORecordDuplicatedException
    public void testDeleteUser()
            throws Exception
    {
        // create new user
        User test = buildUser("test-delete", "password-update");

        given().contentType("application/json")
               .param("juser", test)
               .when()
               .post("/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(200) // check http status code
               .extract()
               .asString();

        given().contentType("application/json")
               .param("The name of the user", test.getUsername())
               .when()
               .delete("/users/user/" + test.getUsername())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(200) // check http status code
               .extract()
               .asString();

    }

    private User retrieveUserByName(String name)
            throws IOException
    {
        return userService.findByUserName(name);
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

}