package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.users.domain.User;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
public class UserControllerTest
        extends BackendBaseTest
{

    private static final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);

    @Inject
    ObjectMapper objectMapper;

    @Test
    @WithUserDetails("admin")
    public void greetTest()
    {

        RestAssuredMockMvc.given()
                          .contentType(ContentType.JSON)
                          .param("The param", "Johan")
                          .when()
                          .get("/users/greet")
                          .then()
                          .statusCode(200)
                          .body(containsString("hello, Johan"));
    }

    @Test
    @WithUserDetails("admin")
    public void testRetrieveUser()
            throws Exception
    {
        final String userName = "admin";

        RestAssuredMockMvc.given()
                          .contentType(ContentType.JSON)
                          .param("The name of the user", userName)
                          .when()
                          .get("/users/user/" + userName)
                          .then()
                          .statusCode(200)
                          .body(containsString("admin"));

    }

    @Test
    @WithUserDetails("admin")
    public void testCreateUser()
            throws Exception
    {
        User test = buildUser("test", "password");

        RestAssuredMockMvc.given()
                          .contentType("application/json")
                          .param("juser", test)
                          .when()
                          .post("/users/user")
                          .peek() // Use peek() to print the ouput
                          .then()
                          .statusCode(200) // check http status code
                          .extract()
                          .asString();
    }

    @Test
    @WithUserDetails("admin")
    public void testRetrieveAllUsers()
            throws Exception
    {

        String response =
                RestAssuredMockMvc.given()
                                  .contentType("application/json")
                                  .when()
                                  .get("/users/all")
                                  .peek() // Use peek() to print the ouput
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
        System.out.println(users);

    }

    @Test
    @WithUserDetails("admin")
    public void testUpdateUser()
            throws Exception
    {
        // create new user
        User test = buildUser("test-update", "password-update");

        RestAssuredMockMvc.given()
                          .contentType("application/json")
                          .param("juser", test)
                          .when()
                          .post("/users/user")
                          .peek() // Use peek() to print the ouput
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

        String response = RestAssuredMockMvc.given()
                                            .contentType("application/json")
                                            .param("juser", createdUser)
                                            .when()
                                            .put("/users/user")
                                            .peek() // Use peek() to print the ouput
                                            .then()
                                            .statusCode(200) // check http status code
                                            .extract()
                                            .asString();

        System.out.println(response);

        // deserialize response
        User updatedUser = objectMapper.readValue(response, User.class);

        assertEquals(createdUser, updatedUser);

    }

    @Test
    @WithUserDetails("admin")
    public void testDeleteUser()
            throws Exception
    {
        // create new user
        User test = buildUser("test-update", "password-update");

        RestAssuredMockMvc.given()
                          .contentType("application/json")
                          .param("juser", test)
                          .when()
                          .post("/users/user")
                          .peek() // Use peek() to print the ouput
                          .then()
                          .statusCode(200) // check http status code
                          .extract()
                          .asString();

        RestAssuredMockMvc.given()
                          .contentType("application/json")
                          .param("The name of the user", test.getUsername())
                          .when()
                          .delete("/users/user/" + test.getUsername())
                          .peek() // Use peek() to print the ouput
                          .then()
                          .statusCode(200) // check http status code
                          .extract()
                          .asString();

    }

    private User retrieveUserByName(String name)
            throws IOException
    {
        String response;

        response = RestAssuredMockMvc.given()
                                     .contentType("application/json")
                                     .param("The name of the user", name)
                                     .when()
                                     .get("/users/user/" + name)
                                     .peek() // Use peek() to print the ouput
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