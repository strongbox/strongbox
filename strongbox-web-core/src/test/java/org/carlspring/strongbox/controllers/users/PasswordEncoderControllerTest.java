package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@SpringBootTest
@Transactional
public class PasswordEncoderControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    PasswordEncoder passwordEncoder;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    @Test
    public void shouldEncodeProperly()
    {

        final String encodedPassword = given().when()
                                              .get("/api/users/password-encoder/password")
                                              .peek()
                                              .then()
                                              .statusCode(200)
                                              .extract()
                                              .asString();

        assertTrue(passwordEncoder.matches("password", encodedPassword));
    }


}
