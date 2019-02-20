package org.carlspring.strongbox.controllers.forms;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@Transactional
public class FormDataControllerTestIT
        extends RestAssuredBaseTest
{

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/api/formData");
    }

    @Test
    public void testGetUserFields()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/userFields")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetStorageFields()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/storageFields")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetStorageNames()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/storageNames")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testFilteredGetStorageNames()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/storageNames?filter=prox")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetRepositoryNames()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/repositoryNames?storageId=storage0")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testFilteredGetRepositoryNames()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/repositoryNames?storageId=storage0&filter=SHOT")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetStorageAndRepositoryIds()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/storageAndRepositoryIds")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(hasItem("public:maven-group")));
    }
}
