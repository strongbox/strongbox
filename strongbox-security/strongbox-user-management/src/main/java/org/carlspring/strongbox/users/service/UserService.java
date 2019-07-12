package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.users.domain.UserData;
import org.carlspring.strongbox.users.domain.Users;
import org.carlspring.strongbox.users.dto.User;
import org.carlspring.strongbox.users.dto.UserDto;

import org.jose4j.lang.JoseException;

/**
 * @author Alex Oreshkevich
 * @author Przemyslaw Fusik
 */
public interface UserService
{

    UserData findByUserName(String username);

    /**
     * Generates another one 'Security Token' for specific user.<br>
     * Token will be based on 'username' with 'securityTokenKey' used as clam.
     *
     * @param username user ID
     * @return encrypted token
     * @throws JoseException
     */
    String generateSecurityToken(String username)
            throws JoseException;

    /**
     * This method is mainly necessary for the UI - for users to be able to update their own account data
     * (i.e. change password or securityToken)
     *
     * @param userToUpdate
     */
    void updateAccountDetailsByUsername(UserDto userToUpdate);

    Users findAll();

    void revokeEveryone(String roleToRevoke);

    void save(User user);

    void delete(String username);

}
