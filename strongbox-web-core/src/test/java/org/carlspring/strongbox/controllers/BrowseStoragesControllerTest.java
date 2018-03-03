package org.carlspring.strongbox.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.testing.TestCaseWithRepository;

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
            throws Exception
    {
        String path = "/browse/";

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
            throws Exception
    {
        String path = "/browse/storage0/";

        RestAssuredMockMvc.given()
                          .header("User-Agent", "unknown/*")
                          .contentType(MediaType.TEXT_PLAIN_VALUE)
                          .when()
                          .get(path)
                          .then()
                          .statusCode(HttpStatus.OK.value());
    }
    
}
