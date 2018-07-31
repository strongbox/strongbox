package org.carlspring.strongbox.controllers.forms;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@RunWith(SpringRunner.class)
@Transactional
public class FormDataControllerTestIT
        extends RestAssuredBaseTest
{

    @Override
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/api/formData");
    }

    @Test
    public void testGetAssignableRoles()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/assignableRoles")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("assignableRoles", notNullValue())
               .body("assignableRoles", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetStoragesFormData()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/storages")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

}
