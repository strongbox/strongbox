package org.carlspring.strongbox.jaas.authentication;

import org.carlspring.strongbox.jaas.Role;
import org.carlspring.strongbox.jaas.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

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
