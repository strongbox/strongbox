package org.carlspring.strongbox.security.jaas.authentication;

import org.carlspring.strongbox.security.jaas.Role;
import org.carlspring.strongbox.security.jaas.User;

/**
 * All login modules should have an implementation of this interface,
 * if they can modify user credentials and privileges.
 * The methods outlined here are as generic as possible and should be kept that way.
 *
 * @author mtodorov
 */
public interface UserStorage
{

    void createUser(User user)
            throws UserStorageException;

    void updateUser(User user)
            throws UserStorageException;

    void removeUser(User user)
            throws UserStorageException;

    void removeUserById(long userId)
            throws UserStorageException;

    void assignRole(User user, Role role)
            throws UserStorageException;

    void assignRole(User user, String roleName)
            throws UserStorageException;

    void removeRole(User user, Role role)
            throws UserStorageException;

    long count()
            throws UserResolutionException;

}
