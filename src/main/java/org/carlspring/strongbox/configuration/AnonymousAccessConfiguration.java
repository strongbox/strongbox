package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.security.jaas.Credentials;
import org.carlspring.strongbox.util.encryption.EncryptionAlgorithms;

/**
 * @author mtodorov
 */
public class AnonymousAccessConfiguration
{

    private boolean enabled = true;

    private String username = "anonymous";

    private Credentials credentials = new Credentials("password", EncryptionAlgorithms.PLAIN.toString());


    public AnonymousAccessConfiguration()
    {
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public Credentials getCredentials()
    {
        return credentials;
    }

    public void setCredentials(Credentials credentials)
    {
        this.credentials = credentials;
    }

}
