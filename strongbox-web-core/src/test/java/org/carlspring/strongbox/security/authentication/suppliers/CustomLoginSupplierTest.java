package org.carlspring.strongbox.security.authentication.suppliers;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.controllers.login.LoginInput;
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

    @Inject
    private JsonFormLoginSupplier customLoginSupplier;

    @Inject
    private ObjectMapper objectMapper;

    @Test
    public void shouldSupportExpectedRequest()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("post", "/api/login");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);

        assertThat(customLoginSupplier.supports(request)).isTrue();
    }

    @Test
    public void shouldNotSupportGetRequest()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("get", "/api/login");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);

        assertThat(customLoginSupplier.supports(request)).isFalse();
    }

    @Test
    public void shouldNotSupportXmlRequest()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("post", "/api/login");
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

        MockHttpServletRequest request = new MockHttpServletRequest("post", "/api/login");
        request.setContent(objectMapper.writeValueAsBytes(loginInput));

        Authentication authentication = customLoginSupplier.supply(request);

        assertThat(authentication).isNotNull();
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getPrincipal()).isEqualTo("przemyslaw");
        assertThat(authentication.getCredentials()).isEqualTo("fusik");
    }

}
