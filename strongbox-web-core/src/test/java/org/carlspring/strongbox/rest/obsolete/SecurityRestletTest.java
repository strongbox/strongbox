package org.carlspring.strongbox.rest.obsolete;

import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.rest.context.JerseyTest;

import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static junit.framework.TestCase.assertEquals;

/**
 * Simple test to make sure that general security functions (anonymous authentication, remember-me) and access
 * restriction works as expected.
 *
 * @author Alex Oreshkevich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@JerseyTest
@Ignore
@Deprecated
public class SecurityRestletTest
{

    private static RestClient client = new RestClient();

    @Autowired
    AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    @AfterClass
    public static void tearDown()
            throws Exception
    {
        if (client != null)
        {
            client.close();
        }
    }

    @Test
    public void testThatAnonymousUserHasFullAccessAccordingToAuthorities()
    {
        anonymousAuthenticationFilter.getAuthorities().add(new SimpleGrantedAuthority("VIEW_USER"));

        Response response = client.prepareUnauthenticatedTarget("/users/user/all").request().get();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        response = client.prepareTarget("/users/greet", "user", "password321").request().get();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }
}
