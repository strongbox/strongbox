package org.carlspring.strongbox.security.authentication.suppliers;

import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.controllers.security.login.LoginInput;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class CustomLoginSupplierTest
{

    @Inject
    private CustomLoginSupplier customLoginSupplier;

    @Inject
    private ObjectMapper objectMapper;

    @Test
    public void shouldSupportExpectedRequest()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("post", "/login");
        request.setContentType("application/json");

        assertTrue(customLoginSupplier.supports(request));
    }

    @Test
    public void shouldNotSupportGetRequest()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("get", "/login");
        request.setContentType("application/json");

        assertFalse(customLoginSupplier.supports(request));
    }

    @Test
    public void shouldNotSupportXmlRequest()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("post", "/login");
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

        MockHttpServletRequest request = new MockHttpServletRequest("post", "/login");
        request.setContent(objectMapper.writeValueAsBytes(loginInput));

        Authentication authentication = customLoginSupplier.supply(request);

        assertThat(authentication, CoreMatchers.notNullValue());
        assertThat(authentication, CoreMatchers.instanceOf(UsernamePasswordAuthenticationToken.class));
        assertThat(authentication.getPrincipal(), CoreMatchers.equalTo("przemyslaw"));
        assertThat(authentication.getCredentials(), CoreMatchers.equalTo("fusik"));
    }

}