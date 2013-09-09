package org.carlspring.strongbox.jaas.caching;

import org.carlspring.strongbox.jaas.Credentials;
import org.carlspring.strongbox.jaas.User;

import java.util.Set;

/**
 * @author mtodorov
 */
public interface UserManager
{

    void addUser(User user);

    void removeUser(String username);

    boolean validCredentials(String username, String password);

    boolean containsUser(String username);

    void removeExpiredCredentials();

    long getCredentialsLifetime();

    void setCredentialsLifetime(long credentialsLifetime);

    long getSize();

}
