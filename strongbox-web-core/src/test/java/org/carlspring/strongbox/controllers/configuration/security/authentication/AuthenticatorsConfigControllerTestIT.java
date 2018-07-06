package org.carlspring.strongbox.controllers.configuration.security.authentication;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.api.impl.xml.PasswordAuthenticator;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import org.hamcrest.CoreMatchers;
import org.junit.After;
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
public class AuthenticatorsConfigControllerTestIT
        extends RestAssuredBaseTest
{

    private static List<Authenticator> originalRegistryList;

    private static final List<Authenticator> registryList = Arrays.asList(new OrientDbAuthenticator(),
                                                                          new LdapAuthenticator());

    @Inject
    private AuthenticatorsRegistry authenticatorsRegistry;

    @Before
    public void setUp()
    {
        Iterator<Authenticator> iterator = authenticatorsRegistry.iterator();
        originalRegistryList = Lists.newArrayList(iterator);
        authenticatorsRegistry.reload(registryList);
    }

    @After
    public void afterEveryTest() {
        authenticatorsRegistry.reload(originalRegistryList);
    }

    @Test
    public void registryShouldReturnExpectedInitialArray()
    {
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/api/configuration/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(OrientDbAuthenticator.class.getName()))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(LdapAuthenticator.class.getName()))
               .body("authenticators.authenticator.size()", is(2))
               .statusCode(HttpStatus.OK.value());
    }

    private void registryShouldBeReloadable(String acceptHeader)
    {
        // Registry should have form setup in test, first
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/api/configuration/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(OrientDbAuthenticator.class.getName()))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(LdapAuthenticator.class.getName()))
               .body("authenticators.authenticator.size()", is(2))
               .statusCode(HttpStatus.OK.value());

        // Reorder elements
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .put("/api/configuration/authenticators/reorder/0/1")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(AuthenticatorsConfigController.SUCCESSFUL_REORDER));

        // Confirm they are re-ordered
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/api/configuration/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(LdapAuthenticator.class.getName()))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(OrientDbAuthenticator.class.getName()))
               .body("authenticators.authenticator.size()", is(2))
               .statusCode(HttpStatus.OK.value());

        // Reload registry
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .put("/api/configuration/authenticators/reload")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(AuthenticatorsConfigController.SUCCESSFUL_RELOAD));

        // Registry should be reloaded
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/api/configuration/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(PasswordAuthenticator.class.getName()))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(org.carlspring.strongbox.authentication.api.impl.ldap.LdapAuthenticator.class.getName()))
               .body("authenticators.authenticator.size()", is(2))
               .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void registryShouldBeReloadableWithResponseInJson()
    {
        registryShouldBeReloadable(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void registryShouldBeReloadableWithResponseInText()
    {
        registryShouldBeReloadable(MediaType.TEXT_PLAIN_VALUE);
    }

    private void registryShouldBeAbleToReorderElement(String acceptHeader)
    {
        // when
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .put("/api/configuration/authenticators/reorder/0/1")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(AuthenticatorsConfigController.SUCCESSFUL_REORDER));

        // then
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/api/configuration/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(LdapAuthenticator.class.getName()))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(OrientDbAuthenticator.class.getName()))
               .body("authenticators.authenticator.size()", is(2))
               .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void registryShouldBeAbleToReorderElementsWithResponseInJson()
    {
        registryShouldBeAbleToReorderElement(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void registryShouldBeAbleToReorderElementsWithResponseInText()
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
