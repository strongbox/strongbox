package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.users.PasswordEncodeForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;

import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.Charset;
import java.util.Locale;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@Transactional
public class PasswordEncoderControllerTest
        extends RestAssuredBaseTest
{

    private static final String UNAUTHORIZED_MESSAGE_CODE = "ExceptionTranslationFilter.insufficientAuthentication";

    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

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
        final PasswordEncodeForm form = new PasswordEncodeForm("password");

        ValidatableMockMvcResponse response = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                                                     .accept(acceptedHeader)
                                                     .body(form)
                                                     .post(getContextBaseUrl())
                                                     .peek()
                                                     .then()
                                                     .statusCode(HttpStatus.OK.value());


        if (acceptedHeader.equals(MediaType.APPLICATION_JSON_VALUE))
        {
            response.body("password", CoreMatchers.not(form.getPassword()));
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
        final PasswordEncodeForm form = new PasswordEncodeForm("password");

        String defaultErrorMessage = messages.getMessage(UNAUTHORIZED_MESSAGE_CODE,
                                                         Locale.ENGLISH);

        String errorMessage = messages.getMessage(UNAUTHORIZED_MESSAGE_CODE,
                                                  defaultErrorMessage);

        String decodedErrorMessage = new String(errorMessage.getBytes(ISO_8859_1),
                                                Charset.defaultCharset());

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
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
        final PasswordEncodeForm form = new PasswordEncodeForm("password");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
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
