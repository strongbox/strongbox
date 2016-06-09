package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.security.encryption.EncryptionAlgorithms;
import org.carlspring.strongbox.security.jaas.Credentials;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author mtodorov
 */
@XmlRootElement (name = "anonymous-access")
@XmlAccessorType(XmlAccessType.FIELD)
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
