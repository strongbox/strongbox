package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class PasswordEncoderControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    PasswordEncoder passwordEncoder;

    @Test
    public void shouldEncodeProperly()
    {

        final String encodedPassword = given().when()
                                              .get("/passwords/password")
                                              .peek()
                                              .then()
                                              .statusCode(200)
                                              .extract()
                                              .asString();

        Assert.assertTrue(passwordEncoder.matches("password", encodedPassword));
    }


}