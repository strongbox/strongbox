package org.carlspring.strongbox.jaas.authentication;

import javax.security.auth.callback.*;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationCallbackHandler
        implements CallbackHandler
{

    private static Logger logger = LoggerFactory.getLogger(AuthenticationCallbackHandler.class);

    private String username;

    private String password;


    public AuthenticationCallbackHandler(String username,
                                         String password)
    {
        this.username = username;
        this.password = password;
    }

    /**
     * Invoke an array of Callbacks.
     * <p/>
     * <p/>
     *
     * @param callbacks an array of <code>Callback</code> objects which contain
     *                  the information requested by an underlying security
     *                  service to be retrieved or displayed.
     * @throws java.io.IOException                                       if an input or output error occurs. <p>
     * @throws javax.security.auth.callback.UnsupportedCallbackException if the implementation of this
     *                                                                   method does not support one or more of the Callbacks
     *                                                                   specified in the <code>callbacks</code> parameter.
     */
    public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException
    {

        for (Callback callback : callbacks)
        {
            if (callback instanceof TextOutputCallback)
            {
                // display the message according to the specified type
                TextOutputCallback toc = (TextOutputCallback) callback;
                switch (toc.getMessageType())
                {
                    case TextOutputCallback.INFORMATION:
                        logger.info(toc.getMessage());
                        break;
                    case TextOutputCallback.ERROR:
                        logger.error(toc.getMessage());
                        break;
                    case TextOutputCallback.WARNING:
                        logger.warn(toc.getMessage());
                        break;
                    default:
                        throw new IOException("Unsupported message type: " + toc.getMessageType());
                }
            }
            else if (callback instanceof NameCallback)
            {
                // prompt the user for a username
                NameCallback nc = (NameCallback) callback;

                // We don't really need to be printing out a prompt from within the test case.
                // System.err.println(nc.getPrompt());

                nc.setName(getUsername());
            }
            else if (callback instanceof PasswordCallback)
            {
                // prompt the user for sensitive information
                PasswordCallback pc = (PasswordCallback) callback;

                // We don't really need to be printing out a prompt from within the test case.
                // System.err.println(pc.getPrompt());

                pc.setPassword(getPassword().toCharArray());
            }
            else
            {
                throw new UnsupportedCallbackException(callback, "Unrecognized callback");
            }
        }
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

}
