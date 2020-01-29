package org.carlspring.strongbox.controllers.login;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.EncodedPasswordUser;
import org.carlspring.strongbox.users.service.impl.OrientDbUserService.OrientDb;

import javax.inject.Inject;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.hamcrest.CoreMatchers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
public class LoginControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    @OrientDb
    private UserService userService;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private MockMvcRequestSpecification mockMvc;

    @Inject
    private SecurityTokenProvider securityTokenProvider;

    @Inject
    protected ConfigurationManager configurationManager;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(LoginController.REQUEST_MAPPING);
    }

    @AfterEach
    public void afterEach()
    {
    }

    private void assertValidToken(LoginOutput loginOutput, Integer timeout)
    {
        // Assert response
        assertThat(loginOutput.getToken()).isNotBlank().matches(Pattern.compile(".*\\..*\\..*"));
        assertThat(loginOutput.getAuthorities().size()).isGreaterThan(0);

        // Token is valid?
        assertThatCode(() -> {
            JwtClaims claims = securityTokenProvider.getClaims(loginOutput.getToken(), true);

            NumericDate issuedAt = claims.getIssuedAt();
            NumericDate expirationTime = claims.getExpirationTime();

            NumericDate expectedExpirationTime = NumericDate.fromSeconds(issuedAt.getValue());
            expectedExpirationTime.addSeconds(timeout);

            assertThat(expirationTime).isEqualTo(expectedExpirationTime);

        }).doesNotThrowAnyException();
    }

    @Test
    public void shouldReturnGeneratedToken()
    {
        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("admin");
        loginInput.setPassword("password");

        // Check if login returns proper response.
        String url = getContextBaseUrl();
        LoginOutput loginOutput = mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                                         .accept(MediaType.APPLICATION_JSON_VALUE)
                                         .body(loginInput)
                                         .when()
                                         .post(url)
                                         .peek()
                                         .then()
                                         .statusCode(HttpStatus.OK.value())
                                         .extract()
                                         .as(LoginOutput.class);

        this.assertValidToken(loginOutput, configurationManager.getSessionTimeoutSeconds());
    }

    @Test
    public void shouldReturnRefreshedToken()
    {
        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("admin");
        loginInput.setPassword("password");

        // Get a token
        String url = getContextBaseUrl();
        LoginOutput loginOutput = mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                                         .accept(MediaType.APPLICATION_JSON_VALUE)
                                         .body(loginInput)
                                         .when()
                                         .post(url)
                                         .peek()
                                         .then()
                                         .statusCode(HttpStatus.OK.value())
                                         .extract()
                                         .as(LoginOutput.class);

        this.assertValidToken(loginOutput, configurationManager.getSessionTimeoutSeconds());

        // Try to refresh
        LoginOutput refreshOutput = mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
                                           .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginOutput.getToken())
                                           .when()
                                           .get(url)
                                           .peek()
                                           .then()
                                           .statusCode(HttpStatus.OK.value())
                                           .extract()
                                           .as(LoginOutput.class);

        this.assertValidToken(refreshOutput, configurationManager.getSessionTimeoutSeconds());

        assertThat(loginOutput.getToken()).isNotEqualTo(refreshOutput.getToken());
    }

    @WithAnonymousUser
    @Test
    public void shouldReturnInvalidCredentialsError()
    {
        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("przemyslaw_fusik");
        loginInput.setPassword("password");

        String url = getContextBaseUrl();
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(loginInput)
               .when()
               .post(url)
               .peek()
               .then()
               .body("error", CoreMatchers.equalTo("invalid.credentials"))
               .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnInvalidCredentialsWhenUserIsDisabled()
    {
        UserDto disabledUser = new UserDto();
        disabledUser.setUsername("test-disabled-user-login");
        disabledUser.setPassword("1234");
        disabledUser.setEnabled(false);
        userService.save(new EncodedPasswordUser(disabledUser, passwordEncoder));

        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("test-disabled-user-login");
        loginInput.setPassword("1234");

        String url = getContextBaseUrl();
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(loginInput)
               .when()
               .post(url)
               .peek()
               .then()
               .body("error", CoreMatchers.equalTo("User account is locked"))
               .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @WithAnonymousUser
    public void userCacheShouldBeClearedAfterPasswordChange()
    {
        UserDto cacheEvictionTestUser = new UserDto();
        cacheEvictionTestUser.setUsername("admin-cache-eviction-test");
        cacheEvictionTestUser.setPassword("password");
        cacheEvictionTestUser.setRoles(ImmutableSet.of("ADMIN"));
        cacheEvictionTestUser.setEnabled(true);
        cacheEvictionTestUser.setSecurityTokenKey("admin-cache-eviction-test-secret");
        userService.save(new EncodedPasswordUser(cacheEvictionTestUser, passwordEncoder));

        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("admin-cache-eviction-test");
        loginInput.setPassword("password");

        String url = getContextBaseUrl();
        LoginOutput loginOutput = mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                                         .accept(MediaType.APPLICATION_JSON_VALUE)
                                         .body(loginInput)
                                         .when()
                                         .post(url)
                                         .peek()
                                         .then()
                                         .statusCode(HttpStatus.OK.value())
                                         .extract()
                                         .as(LoginOutput.class);

        this.assertValidToken(loginOutput, configurationManager.getSessionTimeoutSeconds());

        UserForm userForm = new UserForm();
        userForm.setUsername("admin-cache-eviction-test");
        userForm.setPassword("passwordChanged");

        url = "/api/account";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(userForm)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        url = getContextBaseUrl();
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(loginInput)
               .when()
               .post(url)
               .peek()
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", CoreMatchers.equalTo("invalid.credentials"));
    }

}
