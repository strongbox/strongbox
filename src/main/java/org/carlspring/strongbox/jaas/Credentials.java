package org.carlspring.strongbox.jaas;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

/**
 * @author mtodorov
 */
public class Credentials implements Destroyable
{

    private String password;

    private long lastAccessed;

    private String encryptionAlgorithm;


    public Credentials()
    {
        lastAccessed = System.currentTimeMillis();
    }

    public Credentials(String password)
    {
        this.password = password;
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
