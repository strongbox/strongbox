package org.carlspring.strongbox.security.authorization;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * @author mtodorov
 */
public class AuthorizationException
        extends WebApplicationException
{

    /**
     * Create a HTTP 401 (Unauthorized) exception.
     */
    public AuthorizationException()
    {
        super(Response.status(Status.UNAUTHORIZED).build());
    }

    /**
     * Create a HTTP 401 (Unauthorized) exception.
     *
     * @param message the String that is the entity of the 401 response.
     */
    public AuthorizationException(String message)
    {
        super(Response.status(Status.UNAUTHORIZED).entity(message).type("text/plain").build());
    }

}