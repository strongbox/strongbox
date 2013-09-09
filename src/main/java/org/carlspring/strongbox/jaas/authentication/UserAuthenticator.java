package org.carlspring.strongbox.jaas.authentication;

import org.carlspring.strongbox.jaas.User;
import org.carlspring.strongbox.jaas.caching.CachedUserManager;

import javax.security.auth.login.LoginException;

/**
 * @author mtodorov
 */
public class UserAuthenticator
{

    private CachedUserManager cachedUserManager = new CachedUserManager();


    public UserAuthenticator()
    {
    }

    public User authenticate(String username, String password, UserResolver resolver)
            throws LoginException
    {
        if (cachedUserManager.containsUser(username))
        {
            return cachedUserManager.getUser(username);
        }
        else
        {
            try
            {
                final User user = resolver.findUser(username, password);
                if (user != null)
                {
                    cachedUserManager.addUser(user);
                }

                return user;
            }
            catch (Exception e)
            {
                throw new LoginException(e.getMessage());
            }
        }
    }

    public CachedUserManager getCachedUserManager()
    {
        return cachedUserManager;
    }

    public void setCachedUserManager(CachedUserManager cachedUserManager)
    {
        this.cachedUserManager = cachedUserManager;
    }

}
