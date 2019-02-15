package org.carlspring.strongbox.security.authentication.suppliers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.controllers.login.LoginInput;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        request.setContentType("application/json");

        assertTrue(customLoginSupplier.supports(request));
    }

    @Test
    public void shouldNotSupportGetRequest()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("get", "/api/login");
        request.setContentType("application/json");

        assertFalse(customLoginSupplier.supports(request));
    }

    @Test
    public void shouldNotSupportXmlRequest()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("post", "/api/login");
        request.setContentType("application/xml");

        assertFalse(customLoginSupplier.supports(request));
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

        assertThat(authentication, CoreMatchers.notNullValue());
        assertThat(authentication, CoreMatchers.instanceOf(UsernamePasswordAuthenticationToken.class));
        assertThat(authentication.getPrincipal(), CoreMatchers.equalTo("przemyslaw"));
        assertThat(authentication.getCredentials(), CoreMatchers.equalTo("fusik"));
    }

}
