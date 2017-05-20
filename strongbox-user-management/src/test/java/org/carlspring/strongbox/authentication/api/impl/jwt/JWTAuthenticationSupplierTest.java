package org.carlspring.strongbox.authentication.api.impl.jwt;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import static org.junit.Assert.assertThat;

/**
 * @author Przemyslaw Fusik
 */
public class JWTAuthenticationSupplierTest
{

    private JWTAuthenticationSupplier supplier = new JWTAuthenticationSupplier();

    @Test
    public void shouldSupplyExpectedAuthentication()
    {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer <token>");

        final Authentication authentication = supplier.supply(request);

        assertThat(authentication, Matchers.notNullValue());
        assertThat((JWTAuthentication) authentication, CoreMatchers.isA(JWTAuthentication.class));
        assertThat(((JWTAuthentication) authentication).getToken(), CoreMatchers.equalTo("<token>"));
    }

    @Test
    public void shouldSupplyToNull()
    {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic <token>");

        final Authentication authentication = supplier.supply(request);
        
        assertThat(authentication, Matchers.nullValue());
    }

}