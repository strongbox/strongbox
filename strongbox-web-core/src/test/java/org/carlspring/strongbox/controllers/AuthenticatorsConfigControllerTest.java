package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
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
        RestAssuredMockMvc.when()
                          .get("/configuration/authenticators/")
                          .peek()
                          .then()
                          .body("authenticatorsRegistry.authenticators.authenticator[0].index",
                                CoreMatchers.equalTo("0"))
                          .body("authenticatorsRegistry.authenticators.authenticator[0].name",
                                CoreMatchers.equalTo("org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$OrientDbAuthenticator"))
                          .body("authenticatorsRegistry.authenticators.authenticator[1].index",
                                CoreMatchers.equalTo("1"))
                          .body("authenticatorsRegistry.authenticators.authenticator[1].name",
                                CoreMatchers.equalTo("org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$LdapAuthenticator"))
                          .body("authenticatorsRegistry.authenticators.authenticator.size()", CoreMatchers.is(2))
                          .statusCode(200);
    }

    @Test
    public void registryShouldBeReloadable()
            throws Exception
    {

        // Registry should have form setup in test, first
        RestAssuredMockMvc.when()
                          .get("/configuration/authenticators/")
                          .peek()
                          .then()
                          .body("authenticatorsRegistry.authenticators.authenticator[0].index",
                                CoreMatchers.equalTo("0"))
                          .body("authenticatorsRegistry.authenticators.authenticator[0].name",
                                CoreMatchers.equalTo("org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$OrientDbAuthenticator"))
                          .body("authenticatorsRegistry.authenticators.authenticator[1].index",
                                CoreMatchers.equalTo("1"))
                          .body("authenticatorsRegistry.authenticators.authenticator[1].name",
                                CoreMatchers.equalTo("org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$LdapAuthenticator"))
                          .body("authenticatorsRegistry.authenticators.authenticator.size()", CoreMatchers.is(2))
                          .statusCode(200);

        // Reorder elements
        RestAssuredMockMvc.when()
                          .put("/configuration/authenticators/reorder/0/1")
                          .peek()
                          .then()
                          .statusCode(200);

        // Confirm they are reordered
        RestAssuredMockMvc.when()
                          .get("/configuration/authenticators/")
                          .peek()
                          .then()
                          .body("authenticatorsRegistry.authenticators.authenticator[0].index",
                                CoreMatchers.equalTo("0"))
                          .body("authenticatorsRegistry.authenticators.authenticator[0].name",
                                CoreMatchers.equalTo("org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$LdapAuthenticator"))
                          .body("authenticatorsRegistry.authenticators.authenticator[1].index",
                                CoreMatchers.equalTo("1"))
                          .body("authenticatorsRegistry.authenticators.authenticator[1].name",
                                CoreMatchers.equalTo("org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$OrientDbAuthenticator"))
                          .body("authenticatorsRegistry.authenticators.authenticator.size()", CoreMatchers.is(2))
                          .statusCode(200);

        // Reload registry
        RestAssuredMockMvc.when()
                          .put("/configuration/authenticators/reload")
                          .peek()
                          .then()
                          .statusCode(200);

        // Refistry should be reloaded
        RestAssuredMockMvc.when()
                          .get("/configuration/authenticators/")
                          .peek()
                          .then()
                          .body("authenticatorsRegistry.authenticators.authenticator[0].index",
                                CoreMatchers.equalTo("0"))
                          .body("authenticatorsRegistry.authenticators.authenticator[0].name",
                                CoreMatchers.equalTo("org.carlspring.strongbox.authentication.api.impl.xml.DefaultAuthenticator"))
                          .body("authenticatorsRegistry.authenticators.authenticator[1].index",
                                CoreMatchers.equalTo("1"))
                          .body("authenticatorsRegistry.authenticators.authenticator[1].name",
                                CoreMatchers.equalTo("org.carlspring.strongbox.authentication.api.impl.ldap.LdapAuthenticator"))
                          .body("authenticatorsRegistry.authenticators.authenticator.size()", CoreMatchers.is(2))
                          .statusCode(200);
    }

    @Test
    public void registryShouldBeAbleToReorderElements()
            throws Exception
    {
        // when
        RestAssuredMockMvc.when()
                          .put("/configuration/authenticators/reorder/0/1")
                          .peek()
                          .then()
                          .statusCode(200);

        // then
        RestAssuredMockMvc.when()
                          .get("/configuration/authenticators/")
                          .peek()
                          .then()
                          .body("authenticatorsRegistry.authenticators.authenticator[0].index",
                                CoreMatchers.equalTo("0"))
                          .body("authenticatorsRegistry.authenticators.authenticator[0].name",
                                CoreMatchers.equalTo("org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$LdapAuthenticator"))
                          .body("authenticatorsRegistry.authenticators.authenticator[1].index",
                                CoreMatchers.equalTo("1"))
                          .body("authenticatorsRegistry.authenticators.authenticator[1].name",
                                CoreMatchers.equalTo("org.carlspring.strongbox.controllers.AuthenticatorsConfigControllerTest$OrientDbAuthenticator"))
                          .body("authenticatorsRegistry.authenticators.authenticator.size()", CoreMatchers.is(2))
                          .statusCode(200);
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