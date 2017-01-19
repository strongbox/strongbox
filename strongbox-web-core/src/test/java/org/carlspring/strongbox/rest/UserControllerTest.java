package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.users.domain.User;

import java.io.IOException;
import java.util.List;

import io.restassured.http.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class UserControllerTest
        extends RestAssuredBaseTest
{

    @Test
    public void greetTest()
    {
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

        given().contentType("application/json")
               .param("juser", test)
               .when()
               .post("/users/user")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(200) // check http status code
               .extract()
               .asString();

        // retrieve newly created user and store the id
        User createdUser = retrieveUserByName(test.getUsername());
        assertNotNull("Created user should have id", createdUser.getId());

        // update some property for user
        createdUser.setEnabled(true);

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

        assertEquals(createdUser, updatedUser);
    }
    
    @Test
    public void testGenerateSecurityToken()
        throws Exception
    {
        User user = buildUser("test-update", "password-update");

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
        assertNotNull("Created user should have id", user.getId());

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
                          .get("/users/user/test-update/generate-security-token")
                          .peek() // Use peek() to print the output
                          .then()
                          .statusCode(200) // check http status code
                          .extract()
                          .asString();
        assertEquals(200, response.length());
    }

    @Test
    public void testDeleteUser()
            throws Exception
    {
        // create new user
        User test = buildUser("test-update", "password-update");

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
        String response;

        response = given().contentType("application/json")
                          .param("The name of the user", name)
                          .when()
                          .get("/users/user/" + name)
                          .peek() // Use peek() to print the output
                          .then()
                          .statusCode(200) // check http status code
                          .extract()
                          .asString();

        User admin = objectMapper.readValue(response, User.class);
        return admin;
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