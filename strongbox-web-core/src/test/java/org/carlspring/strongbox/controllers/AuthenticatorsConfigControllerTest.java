package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.CustomMatchers.equalByToString;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class AuthenticatorsConfigControllerTest
        extends RestAssuredBaseTest
{

    private static final List<Authenticator> registryList = Arrays.asList(new OrientDbAuthenticator(),
                                                                          new LdapAuthenticator());

    @Inject
    private AuthenticatorsRegistry authenticatorsRegistry;

    @Before
    public void setUp()
            throws Exception
    {
        authenticatorsRegistry.reload(registryList);
    }

    @Test
    public void registryShouldReturnExpectedInitialArray()
            throws Exception
    {
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/configuration/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(
                             "org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$OrientDbAuthenticator"))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(
                             "org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$LdapAuthenticator"))
               .body("authenticators.authenticator.size()", is(2))
               .statusCode(HttpStatus.OK.value());
    }

    private void registryShouldBeReloadable(String acceptHeader)
            throws Exception
    {

        // Registry should have form setup in test, first
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/configuration/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(
                             "org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$OrientDbAuthenticator"))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(
                             "org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$LdapAuthenticator"))
               .body("authenticators.authenticator.size()", is(2))
               .statusCode(HttpStatus.OK.value());

        // Reorder elements
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .put("/configuration/authenticators/reorder/0/1")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(AuthenticatorsConfigController.SUCCESSFUL_REORDER));

        // Confirm they are reordered
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/configuration/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(
                             "org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$LdapAuthenticator"))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(
                             "org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$OrientDbAuthenticator"))
               .body("authenticators.authenticator.size()", is(2))
               .statusCode(HttpStatus.OK.value());

        // Reload registry
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .put("/configuration/authenticators/reload")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(AuthenticatorsConfigController.SUCCESSFUL_RELOAD));

        // Registry should be reloaded
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/configuration/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(
                             "org.carlspring.strongbox.authentication.api.impl.xml.DefaultAuthenticator"))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(
                             "org.carlspring.strongbox.authentication.api.impl.ldap.LdapAuthenticator"))
               .body("authenticators.authenticator.size()", is(2))
               .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void registryShouldBeReloadableWithResponseInJson()
            throws Exception
    {
        registryShouldBeReloadable(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void registryShouldBeReloadableWithResponseInText()
            throws Exception
    {
        registryShouldBeReloadable(MediaType.TEXT_PLAIN_VALUE);
    }

    private void registryShouldBeAbleToReorderElement(String acceptHeader)
            throws Exception
    {
        // when
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .put("/configuration/authenticators/reorder/0/1")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(AuthenticatorsConfigController.SUCCESSFUL_REORDER));

        // then
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/configuration/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(
                             "org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$LdapAuthenticator"))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(
                             "org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$OrientDbAuthenticator"))
               .body("authenticators.authenticator.size()", is(2))
               .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void registryShouldBeAbleToReorderElementsWithResponseInJson()
            throws Exception
    {
        registryShouldBeAbleToReorderElement(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void registryShouldBeAbleToReorderElementsWithResponseInText()
            throws Exception
    {
        registryShouldBeAbleToReorderElement(MediaType.TEXT_PLAIN_VALUE);
    }

    private static class OrientDbAuthenticator
            implements Authenticator
    {

        @Nonnull
        @Override
        public AuthenticationProvider getAuthenticationProvider()
        {
            return new AuthenticationProvider()
            {
                @Override
                public Authentication authenticate(Authentication authentication)
                        throws AuthenticationException
                {
                    return authentication;
                }

                @Override
                public boolean supports(Class<?> authentication)
                {
                    return true;
                }
            };

        }
    }

    private static class LdapAuthenticator
            implements Authenticator
    {

        @Nonnull
        @Override
        public AuthenticationProvider getAuthenticationProvider()
        {
            return new AuthenticationProvider()
            {
                @Override
                public Authentication authenticate(Authentication authentication)
                        throws AuthenticationException
                {
                    return authentication;
                }

                @Override
                public boolean supports(Class<?> authentication)
                {
                    return true;
                }
            };
        }
    }

}
