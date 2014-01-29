package org.carlspring.strongbox.jaas.authentication;

import org.carlspring.strongbox.jaas.Role;
import org.carlspring.strongbox.jaas.User;

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
            throws SQLException;

    void updateUser(User user)
            throws SQLException;

    void removeUser(User user)
            throws SQLException;

    void removeUserById(long userId)
            throws SQLException;

    void assignRole(User user, Role role)
            throws SQLException;

    void assignRole(User user, String roleName)
            throws SQLException;

    Set<Role> getRoles(User user)
            throws SQLException;

    void removeRole(User user, Role role)
            throws SQLException;

    boolean hasRole(User user, String roleName)
            throws SQLException;

}
