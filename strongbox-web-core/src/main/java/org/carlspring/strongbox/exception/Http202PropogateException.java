/**
 * @author Bogdan Sukonnov
 * 
 */
package org.carlspring.strongbox.exception;

import java.io.IOException;

/**
 * @author Bogdan Sukonnov
 *
 */
public class Http202PropogateException extends IOException
{

    public Http202PropogateException(final String message)
    {
        super(message);
    }    

}
