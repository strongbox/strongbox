package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.domain.Users;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.dto.UserReadContract;
import org.jose4j.lang.JoseException;

/**
 * @author Alex Oreshkevich
 * @author Przemyslaw Fusik
 */
public interface UserService
{

    User findByUserName(String username);

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
     * Generates 'Authentication Token' for specific user.<br>
     * This token can be used for JWT Authentication.
     *
     * @param username      user ID
     * @param expireSeconds token expiration in seconds (endless if empty)
     * @return encrypted token
     * @throws JoseException
     */
    String generateAuthenticationToken(String username,
                                       Integer expireSeconds)
            throws JoseException;

    void updatePassword(UserDto userToUpdate);

    void updateSecurityToken(UserDto userToUpdate);

    /**
     * This method is mainly necessary for the UI - for users to be able to update their own account data
     * (i.e. change password or securityToken)
     *
     * @param userToUpdate
     */
    void updateAccountDetailsByUsername(UserDto userToUpdate);

    Users findAll();

    void revokeEveryone(String roleToRevoke);

    void save(UserReadContract user);

    void delete(String username);

    void updateAccessModel(String username,
                           UserAccessModelDto accessModel);
}
