package org.carlspring.strongbox.jaas.authentication.basic;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP Basic decoder.
 *
 * @author mtodorov
 */
public class BasicAuthenticationDecoder
{

    /**
     * Decode the basic auth and convert it to array login/password.
     *
     * @param authentication    The encoded authentication
     * @return                  The login (case 0), the password (case 1)
     */
    public static String[] decode(String authentication)
    {
        // Remove the "basic" part
        authentication = authentication.split(" ")[1];

        byte[] bytes = DatatypeConverter.parseBase64Binary(authentication);
        if (bytes == null || bytes.length == 0)
        {
            return null;
        }

        // Split the username:password into and put into a String array
        return new String(bytes).split(":", 2);
    }

}