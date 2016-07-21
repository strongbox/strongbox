package org.carlspring.strongbox.rest;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.jayway.restassured.module.mockmvc.response.MockMvcResponse;
import com.jayway.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.ResponseBody;
import com.jayway.restassured.response.ResponseBodyExtractionOptions;
import org.apache.http.HttpStatus;
import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.users.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

import static org.carlspring.strongbox.rest.CustomJerseyTest.objectMapper;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

/**
 * Created by yury on 18.7.16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
public class UserControllerTest  extends BackendBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(UserRestletTest.class);

    @Test
    @WithUserDetails("admin")
    public void greetTest() {

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
            throws Exception {
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

        RestAssuredMockMvc.given().
                contentType(ContentType.JSON).
                param("juser", test).
                when().
                post("/users/user").
                then().
                statusCode(200).
                body("statusInfo",containsString("OK"));
    }

    @Test
    @WithUserDetails("admin")
    public void testRetrieveAllUsers()
            throws Exception
    {

      String response;

        response = RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/users/all")
                .then()
                .statusCode(200)
                .body("statusInfo",containsString("OK"))
                .extract()
                .response()
                .body().path("entity");

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

        RestAssuredMockMvc.given().
                contentType(ContentType.JSON).
                param("juser", test).
                when().
                post("/users/user").
                then().
                statusCode(200).
                body("statusInfo",containsString("OK"));

        // retrieve newly created user and store the id
        User createdUser = retrieveUserByName(test.getUsername());
        assertNotNull("Created user should have id", createdUser.getId());

        // update some property for user
        createdUser.setEnabled(true);

        // send update request
        String response;

        response = RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .param("juser", createdUser)
                .when()
                .put("/users/user")
                .then()
                .statusCode(200)
                .body("statusInfo",containsString("OK"))
                .extract()
                .response()
                .body()
                .path("entity");

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

        RestAssuredMockMvc.given().
                contentType(ContentType.JSON).
                param("juser", test).
                when().
                post("/users/user").
                then().
                statusCode(200).
                body("statusInfo",containsString("OK"));

        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .param("The name of the user", test.getUsername())
                .when()
                .delete("/users/user/" + test.getUsername())
                .then()
                .statusCode(200)
                .body("statusInfo",containsString("OK"));
    }

    private User retrieveUserByName(String name)
            throws IOException
    {
        String response;

        response = RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .param("The name of the user", name)
                .when()
                .get("/users/user/" + name)
                .then()
                .statusCode(200)
                .body(containsString(name))
                .extract()
                .response()
                .body()
                .path("entity");

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