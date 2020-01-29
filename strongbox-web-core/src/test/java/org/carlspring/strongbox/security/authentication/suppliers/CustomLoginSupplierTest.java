package org.carlspring.strongbox.security.authentication.suppliers;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.controllers.login.LoginInput;
import io.restassured.http.Method;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
public class CustomLoginSupplierTest
{

    private static final String REQUEST_URI = "/api/login";

    @Inject
    private JsonFormLoginSupplier customLoginSupplier;

    @Inject
    private ObjectMapper objectMapper;

    @Test
    public void shouldSupportExpectedRequest()
    {
        MockHttpServletRequest request = new MockHttpServletRequest(Method.POST.name(), REQUEST_URI);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);

        assertThat(customLoginSupplier.supports(request)).isTrue();
    }

    @Test
    public void shouldNotSupportGetRequest()
    {
        MockHttpServletRequest request = new MockHttpServletRequest(Method.GET.name(), REQUEST_URI);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);

        assertThat(customLoginSupplier.supports(request)).isFalse();
    }

    @Test
    public void shouldNotSupportXmlRequest()
    {
        MockHttpServletRequest request = new MockHttpServletRequest(Method.POST.name(), REQUEST_URI);
        request.setContentType(MediaType.APPLICATION_XML_VALUE);

        assertThat(customLoginSupplier.supports(request)).isFalse();
    }

    @Test
    public void shouldSupply()
            throws Exception
    {
        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("przemyslaw");
        loginInput.setPassword("fusik");

        MockHttpServletRequest request = new MockHttpServletRequest(Method.POST.name(), REQUEST_URI);
        request.setContent(objectMapper.writeValueAsBytes(loginInput));

        Authentication authentication = customLoginSupplier.supply(request);

        assertThat(authentication).isNotNull();
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getPrincipal()).isEqualTo("przemyslaw");
        assertThat(authentication.getCredentials()).isEqualTo("fusik");
    }

}
