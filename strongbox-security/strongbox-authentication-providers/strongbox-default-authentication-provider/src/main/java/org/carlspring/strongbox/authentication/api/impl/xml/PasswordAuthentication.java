package org.carlspring.strongbox.authentication.api.impl.xml;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * @author Sergey Bespalov
 *
 */
public class PasswordAuthentication extends UsernamePasswordAuthenticationToken
{

    public PasswordAuthentication(Object principal,
                                  Object credentials)
    {
        super(principal, credentials);
    }

}
