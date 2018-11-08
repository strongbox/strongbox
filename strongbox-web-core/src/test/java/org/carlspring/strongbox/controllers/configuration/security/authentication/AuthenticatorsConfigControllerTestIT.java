package org.carlspring.strongbox.controllers.configuration.security.authentication;

import org.carlspring.strongbox.authentication.api.impl.xml.JwtAuthenticationProvider;
import org.carlspring.strongbox.authentication.api.impl.xml.PasswordAuthenticationProvider;
import org.carlspring.strongbox.authentication.api.impl.xml.SecurityTokenAuthenticationProvider;
import org.carlspring.strongbox.authentication.registry.AuthenticationProvidersRegistry;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.CustomMatchers.equalByToString;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class AuthenticatorsConfigControllerTestIT
        extends RestAssuredBaseTest
{

    private static List<AuthenticationProvider> originalRegistryList;

    private static final List<AuthenticationProvider> registryList = Arrays.asList(new OrientDbAuthenticationProvider(),
                                                                          new LdapAuthenticationProvider());

    @Inject
    private AuthenticationProvidersRegistry authenticationProvidersRegistry;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/api/configuration");
        
        Iterator<AuthenticationProvider> iterator = authenticationProvidersRegistry.iterator();
        originalRegistryList = Lists.newArrayList(iterator);
        authenticationProvidersRegistry.reload(registryList);
    }

    @AfterEach
    public void afterEveryTest() {
        authenticationProvidersRegistry.reload(originalRegistryList);
    }

    @Test
    public void registryShouldReturnExpectedInitialArray()
    {
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(OrientDbAuthenticationProvider.class.getName()))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(LdapAuthenticationProvider.class.getName()))
               .body("authenticators.authenticator.size()", is(2))
               .statusCode(HttpStatus.OK.value());
    }

    private void registryShouldBeReloadable(String acceptHeader)
    {
        // Registry should have form setup in test, first
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(OrientDbAuthenticationProvider.class.getName()))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(LdapAuthenticationProvider.class.getName()))
               .body("authenticators.authenticator.size()", is(2))
               .statusCode(HttpStatus.OK.value());

        // Reorder elements
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .put(getContextBaseUrl() + "/authenticators/reorder/0/1")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(AuthenticatorsConfigController.SUCCESSFUL_REORDER));

        // Confirm they are re-ordered
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(LdapAuthenticationProvider.class.getName()))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(OrientDbAuthenticationProvider.class.getName()))
               .body("authenticators.authenticator.size()", is(2))
               .statusCode(HttpStatus.OK.value());

        // Reload registry
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .put(getContextBaseUrl() + "/authenticators/reload")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(AuthenticatorsConfigController.SUCCESSFUL_RELOAD));

        // Registry should be reloaded
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(PasswordAuthenticationProvider.class.getName()))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(JwtAuthenticationProvider.class.getName()))
               .body("authenticators.authenticator[2].name",
                     CoreMatchers.equalTo(SecurityTokenAuthenticationProvider.class.getName()))
//               .body("authenticators.authenticator[3].name",
//                     CoreMatchers.equalTo(org.carlspring.strongbox.authentication.api.impl.ldap.LdapAuthenticationProvider.class.getName()))               
               .body("authenticators.authenticator.size()", is(4))
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
               .put(getContextBaseUrl() + "/authenticators/reorder/0/1")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(AuthenticatorsConfigController.SUCCESSFUL_REORDER));

        // then
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/authenticators/")
               .peek()
               .then()
               .body("authenticators.authenticator[0].index",
                     equalByToString(0))
               .body("authenticators.authenticator[0].name",
                     CoreMatchers.equalTo(LdapAuthenticationProvider.class.getName()))
               .body("authenticators.authenticator[1].index",
                     equalByToString(1))
               .body("authenticators.authenticator[1].name",
                     CoreMatchers.equalTo(OrientDbAuthenticationProvider.class.getName()))
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

    private static class OrientDbAuthenticationProvider
            implements AuthenticationProvider
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
    }

    private static class LdapAuthenticationProvider
            implements AuthenticationProvider
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
    }

}
