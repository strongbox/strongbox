package org.carlspring.strongbox.security;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Credentials
        implements Destroyable
{

    @XmlElement(name = "password")
    private String password;

    @XmlTransient
    private long lastAccessed;

    @XmlElement(name = "encryption-algorithm")
    private String encryptionAlgorithm;


    public Credentials()
    {
        this.lastAccessed = System.currentTimeMillis();
    }

    public Credentials(String password)
    {
        this.password = password;
        this.lastAccessed = System.currentTimeMillis();
    }

    public Credentials(String password, String encryptionAlgorithm)
    {
        this.password = password;
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.lastAccessed = System.currentTimeMillis();
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public long getLastAccessed()
    {
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed)
    {
        this.lastAccessed = lastAccessed;
    }

    public String getEncryptionAlgorithm()
    {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm)
    {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    @Override
    public void destroy()
            throws DestroyFailedException
    {
        password = null;
    }

    @Override
    public boolean isDestroyed()
    {
        return password == null;
    }

}
