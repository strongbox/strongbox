package org.carlspring.strongbox.jaas.authentication;

import org.carlspring.strongbox.jaas.User;

/**
 * All login modules should have an implementation of this interface.
 * The methods outlined here are as generic as possible and should be kept that way.
 *
 * The purpose of this interface is to provide a means of resolving users.
 * Certain authentication providers like the ones for LDAP and AD will not support write operations.
 * These are covered by UserStorage.
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
            throws UserResolutionException;

    /**
     * Find a user by their username.
     *
     * @param username
     * @return
     * @throws Exception
     */
    User findUser(String username)
            throws UserResolutionException;

    /**
     * Check if a user's username and password are valid for logging in.
     *
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    User findUser(String username, String password)
            throws UserResolutionException;

}
