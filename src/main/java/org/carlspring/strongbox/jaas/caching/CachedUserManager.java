package org.carlspring.strongbox.jaas.caching;

import org.carlspring.strongbox.jaas.Credentials;
import org.carlspring.strongbox.jaas.User;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class CachedUserManager implements UserManager
{

    private static Logger logger = LoggerFactory.getLogger(CachedUserManager.class);

    private Map<String, User> cachedUsers = new LinkedHashMap<String, User>();

    /**
     * Specifies how long to keep the cached credentials.
     *
     * Don't set this value too high, as the user might, for example, change their password
     * and it will take a while for that to be refreshed, if this is set to a high value.
     *
     * The default is five minutes.
     */
    private long credentialsLifetime = 5 * 60000;

    /**
     * Specifies at what interval to check if the credentials have expired.
     * The default is to check once every minute.
     */
    private long credentialExpiredCheckInterval = 60000l;


    public CachedUserManager()
    {
        new CachedCredentialsExpirer();
    }

    public boolean containsUser(String username)
    {
        if (cachedUsers.containsKey(username))
        {
            logger.debug("Cache contains user '" + username + "'.");
        }

        return cachedUsers.containsKey(username);
    }

    public User getUser(String username)
    {
        if (cachedUsers.get(username) != null)
        {
            logger.debug("Found user '" + username + "' in cache.");
        }

        return cachedUsers.get(username);
    }

    public synchronized void addUser(User user)
    {
        logger.debug("Adding user '" + user + "' to cache.");
        cachedUsers.put(user.getUsername(), user);
    }

    public synchronized void removeUser(String username)
    {
        logger.debug("Removing user '" + username + "' from cache.");
        cachedUsers.remove(username);
    }

    public boolean validCredentials(String username,
                                    String password)
    {
        final Credentials credentials = cachedUsers.get(username).getCredentials();
        credentials.setLastAccessed(System.currentTimeMillis());

        return credentials.getPassword().equals(password);
    }

    public synchronized void removeExpiredCredentials()
    {
        for (String username : getExpiredCredentials())
        {
            removeUser(username);
        }
    }

    private Set<String> getExpiredCredentials()
    {
        Set<String> expiredCredentials = new LinkedHashSet<String>();

        for (String username : cachedUsers.keySet())
        {
            Credentials credentials = cachedUsers.get(username).getCredentials();
            if (System.currentTimeMillis() - credentials.getLastAccessed() > credentialsLifetime)
            {
                expiredCredentials.add(username);
            }
        }

        return expiredCredentials;
    }

    public long getCredentialsLifetime()
    {
        return credentialsLifetime;
    }

    public void setCredentialsLifetime(long credentialsLifetime)
    {
        this.credentialsLifetime = credentialsLifetime;
    }

    public long getCredentialExpiredCheckInterval()
    {
        return credentialExpiredCheckInterval;
    }

    public void setCredentialExpiredCheckInterval(long credentialExpiredCheckInterval)
    {
        this.credentialExpiredCheckInterval = credentialExpiredCheckInterval;
    }

    public long getSize()
    {
        return cachedUsers.size();
    }

    private class CachedCredentialsExpirer
            extends Thread
    {

        private CachedCredentialsExpirer()
        {
            start();
        }

        @Override
        public void run()
        {
            try
            {
                //noinspection InfiniteLoopStatement
                while (true)
                {
                    removeExpiredCredentials();
                    Thread.sleep(getCredentialExpiredCheckInterval());
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

}
