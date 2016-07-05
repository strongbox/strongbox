package org.carlspring.strongbox.rest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Example of how to write unit tests for REST API.
 *
 * @author Alex Oreshkevich
 */
public class JerseyExampleTest
        extends CustomJerseyTest
{

    // simplest possible test; just to make sure that unit test configuration is correct
    // didn't call any rest api endpoint
    @Test
    public void simplestTest()
    {
        System.out.println("OK");
    }

    // test secured rest api endpoint
    @Test
    public void greetingServerTest()
            throws Exception
    {
        final String message = requestApi("/users/greet").get(String.class);
        assertEquals("\"hello, greet\"", message);
    }
}
