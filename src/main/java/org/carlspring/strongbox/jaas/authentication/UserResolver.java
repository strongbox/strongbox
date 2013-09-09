package org.carlspring.strongbox.jaas.authentication;

import org.carlspring.strongbox.jaas.User;

/**
 * All login modules should have an implementation of this interface.
 * The methods outlined here are as generic as possible and should be kept that way.
 *
 * @author mtodorov
 */
public interface UserResolver
{

    /**
     * Find a user by their id.
     *
     * @param userId
     * @return
     * @throws Exception
     */
    User findUser(long userId)
            throws Exception;

    /**
     * Find a user by their username.
     *
     * @param username
     * @return
     * @throws Exception
     */
    User findUser(String username)
            throws Exception;

    /**
     * Check if a user's username and password are valid for logging in.
     *
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    User findUser(String username, String password)
            throws Exception;

}
