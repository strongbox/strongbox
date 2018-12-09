package org.carlspring.strongbox.controllers.configuration.security.authentication;

import org.carlspring.strongbox.authentication.ConfigurableProviderManager;
import org.carlspring.strongbox.authentication.api.AuthenticationItem;
import org.carlspring.strongbox.authentication.api.AuthenticationItems;
import org.carlspring.strongbox.authentication.registry.AuthenticationResourceManager;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.config.hazelcast.HazelcastConfiguration;
import org.carlspring.strongbox.config.hazelcast.HazelcastInstanceId;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ActiveProfiles;

import static org.carlspring.strongbox.CustomMatchers.equalByToString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;


/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 * @author sbespalov
 */
@ActiveProfiles({ "test",
                  "AuthenticatorsConfigControllerTestConfig" })
@IntegrationTest
public class AuthenticatorsConfigControllerTestIT
        extends RestAssuredBaseTest
{

    @Inject
    private ConfigurableProviderManager configurableProviderManager;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl("/api/configuration/authenticators");
    }

    @AfterEach
    public void afterEveryTest()
            throws IOException
    {
        configurableProviderManager.reload();
    }

    @Test
    public void registryShouldReturnExpectedInitialArray()
    {
        assertInitialAuthenticationItems();
    }

    private void assertInitialAuthenticationItems()
    {
        String url = getContextBaseUrl();
        mockMvc.when()
               .get(url)
               .peek()
               .then()
               .body("authenticationItemList[0].name",
                     equalByToString("authenticationProviderFirst"))
               .body("authenticationItemList[0].order",
                     equalByToString("0"))
               .body("authenticationItemList[0].enabled",
                     equalByToString("true"))
               .body("authenticationItemList[1].name",
                     equalByToString("authenticationProviderSecond"))
               .body("authenticationItemList[1].order",
                     equalByToString("1"))
               .body("authenticationItemList[1].enabled",
                     equalByToString("true"))
               .body("authenticationItemList[2].name",
                     equalByToString("authenticationProviderThird"))
               .body("authenticationItemList[2].order",
                     equalByToString("2"))
               .body("authenticationItemList[2].enabled",
                     equalByToString("false"))
               .body("authenticationItemList[3].name",
                     equalByToString("yamlUserDetailService"))
               .body("authenticationItemList[3].order",
                     equalByToString("0"))
               .body("authenticationItemList.size()", is(4))
               .statusCode(HttpStatus.OK.value());
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void registryShouldBeReloadable(String acceptHeader)
    {
        assertInitialAuthenticationItems();

        String url = getContextBaseUrl() + "/reorder/{first}/{second}";
        mockMvc.accept(acceptHeader)
               .when()
               .put(url, "authenticationProviderFirst", "authenticationProviderSecond")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(AuthenticatorsConfigController.SUCCESSFUL_REORDER));

        // Confirm they are re-ordered
        url = getContextBaseUrl();
        mockMvc.when()
               .get(url)
               .peek()
               .then()
               .body("authenticationItemList[0].name",
                     equalByToString("authenticationProviderSecond"))
               .body("authenticationItemList[0].order",
                     equalByToString("0"))
               .body("authenticationItemList[1].name",
                     equalByToString("authenticationProviderFirst"))
               .body("authenticationItemList[1].order",
                     equalByToString("1"))
               .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void authenticationItemCanBeEnabled()
    {
        assertInitialAuthenticationItems();

        AuthenticationItem authenticationItem = new AuthenticationItem("authenticationProviderThird",
                                                                       AuthenticationProvider.class.getSimpleName());
        authenticationItem.setEnabled(true);
        authenticationItem.setOrder(2);

        AuthenticationItems authenticationItems = new AuthenticationItems();
        authenticationItems.getAuthenticationItemList().add(authenticationItem);

        String url = getContextBaseUrl();
        mockMvc.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
               .body(authenticationItems)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(AuthenticatorsConfigController.SUCCESSFUL_UPDATE));

        mockMvc.when()
               .get(url)
               .peek()
               .then()
               .body("authenticationItemList[2].name",
                     equalByToString("authenticationProviderThird"))
               .body("authenticationItemList[2].enabled",
                     equalByToString("true"))
               .statusCode(HttpStatus.OK.value());
    }

    @Profile("AuthenticatorsConfigControllerTestConfig")
    @Import(HazelcastConfiguration.class)
    @Configuration
    public static class AuthenticatorsConfigControllerTestConfig
    {

        @Primary
        @Bean
        public HazelcastInstanceId hazelcastInstanceIdAcctit()
        {
            return new HazelcastInstanceId("AuthenticatorsConfigControllerTestConfig-hazelcast-instance");
        }

        @Bean
        @Primary
        public AuthenticationResourceManager testAuthenticationResourceManager()
        {
            return new TestAuthenticationResourceManager();
        }

    }

    private static class TestAuthenticationResourceManager
            extends AuthenticationResourceManager
    {

        @Override
        public Resource getAuthenticationConfigurationResource()
        {
            return new DefaultResourceLoader().getResource("classpath:accit-authentication-providers.xml");
        }

        @Override
        public Resource getAuthenticationPropertiesResource()
        {
            return new DefaultResourceLoader().getResource("classpath:accit-authentication-providers.yaml");
        }

    }

    static class TestAuthenticationProvider
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
