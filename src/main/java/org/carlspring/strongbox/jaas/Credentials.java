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
