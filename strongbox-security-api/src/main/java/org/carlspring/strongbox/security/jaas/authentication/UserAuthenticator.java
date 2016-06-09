package org.carlspring.strongbox.security.jaas.authentication;

import org.carlspring.strongbox.security.jaas.User;
import org.carlspring.strongbox.security.jaas.caching.CachedUserManager;

import javax.security.auth.login.LoginException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class UserAuthenticator
{

    @Autowired
    private CachedUserManager cachedUserManager;


    public UserAuthenticator()
    {
    }

    public User authenticate(String username, String password, UserResolver resolver)
            throws LoginException
    {
        if (getCachedUserManager().containsUser(username))
        {
            final User user = getCachedUserManager().getUser(username);
            if (user.getPassword().equals(password))
            {
                return user;
            }
            else
            {
                return null;
            }
        }
        else
        {
            try
            {
                final User user = resolver.findUser(username, password);
                if (user != null)
                {
                    getCachedUserManager().addUser(user);
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
