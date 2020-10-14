package org.carlspring.strongbox.controllers.users;

import static org.carlspring.strongbox.db.schema.Properties.PASSWORD;

import javax.inject.Inject;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.users.PasswordEncodeForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
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
        setContextBaseUrl(getContextBaseUrl() + "/api/users/encrypt/password");
    }

    @ParameterizedTest
    @WithUserDetails("admin")
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    public void shouldEncodeProperly(String acceptedHeader)
    {
        final PasswordEncodeForm form = new PasswordEncodeForm(PASSWORD);

        ValidatableMockMvcResponse response = mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                                                     .accept(acceptedHeader)
                                                     .body(form)
                                                     .post(getContextBaseUrl())
                                                     .peek()
                                                     .then()
                                                     .statusCode(HttpStatus.OK.value());


        if (acceptedHeader.equals(MediaType.APPLICATION_JSON_VALUE))
        {
            response.body(PASSWORD, CoreMatchers.not(form.getPassword()));
        }
        else
        {
            response.body(CoreMatchers.not(form.getPassword()));
        }
    }

    @Test
    @WithAnonymousUser
    public void shouldRequireAuthenticationAccess()
    {
        final PasswordEncodeForm form = new PasswordEncodeForm(PASSWORD);

        String decodedErrorMessage = getI18nInsufficientAuthenticationErrorMessage();

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(form)
               .when()
               .post(getContextBaseUrl())
               .peek()
               .then()
               .body("error", CoreMatchers.containsString(decodedErrorMessage))
               .statusCode(HttpStatus.UNAUTHORIZED.value());
    }


    @Test
    @WithUserDetails("deployer")
    public void shouldAllowOnlyAdminAccess()
    {
        final PasswordEncodeForm form = new PasswordEncodeForm(PASSWORD);

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(form)
               .when()
               .post(getContextBaseUrl())
               .peek()
               .then()
               .body("error", CoreMatchers.containsString("forbidden"))
               .statusCode(HttpStatus.FORBIDDEN.value());
    }

}
