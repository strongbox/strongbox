package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.data.service.CrudService;
import org.carlspring.strongbox.users.domain.User;

import org.jose4j.lang.JoseException;

/**
 * CRUD service for managing {@link User} entities.
 *
 * @author Alex Oreshkevich
 */
public interface UserService
        extends CrudService<User, String>
{

    User findByUserName(final String username);

    /**
     * Generates another one 'Security Token' for specific user.<br>
     * Token will be based on 'username' with 'securityTokenKey' used as clam.
     *
     * @param username
     *            user ID
     * @return encrypted token
     * @throws JoseException
     */
    String generateSecurityToken(String username)
            throws JoseException;

    /**
     * Generates 'Authentication Token' for specific user.<br>
     * This token can be used for JWT Authentication.
     *
     * @param username     user ID
     * @param expireSeconds token expiration in seconds (endless if empty)
     * @return encrypted token
     * @throws JoseException
     */
    String generateAuthenticationToken(String username,
                                       Integer expireSeconds)
            throws JoseException;

    /**
     * @param username
     * @param token
     */
    void verifySecurityToken(String username,
                             String token);

    User updatePassword(User userToUpdate);

    User updateByUsername(User userToUpdate);
}
