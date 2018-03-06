package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.testing.TestCaseWithRepository;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author sanket407
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class BrowseStoragesControllerTest
        extends TestCaseWithRepository
{
    
    
    @Test
    public void testBrowseStorages()
    {
        String path = "/api/browse/";

        RestAssuredMockMvc.given()
                          .header("User-Agent", "unknown/*")
                          .contentType(MediaType.TEXT_PLAIN_VALUE)
                          .when()
                          .get(path)
                          .then()
                          .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void testBrowseRepositoriesInStorages()
    {
        String path = "/api/browse/storage0/";

        RestAssuredMockMvc.given()
                          .header("User-Agent", "unknown/*")
                          .contentType(MediaType.TEXT_PLAIN_VALUE)
                          .when()
                          .get(path)
                          .then()
                          .statusCode(HttpStatus.OK.value());
    }
    
}
