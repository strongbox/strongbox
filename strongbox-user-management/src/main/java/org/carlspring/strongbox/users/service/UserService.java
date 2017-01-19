package org.carlspring.strongbox.users.service;

import java.util.Date;

import org.carlspring.strongbox.data.service.CrudService;
import org.carlspring.strongbox.users.domain.User;
import org.jose4j.lang.JoseException;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD service for managing {@link User} entities.
 *
 * @author Alex Oreshkevich
 */
@Transactional
public interface UserService
        extends CrudService<User, String>
{

    @Transactional
    User findByUserName(final String username);

    /**
     * Generates another one 'Security Token' for specific user.<br>
     * Token will be based on 'username' with 'securityTokenKey' used as clam.
     * 
     * @param id
     *            user ID
     * @param expire
     *            token expiration date
     * @return encrypted token
     * @throws JoseException
     */
    String generateSecurityToken(String id,
                                 Date expire)
        throws JoseException;

    /**
     * Generates 'Authentication Token' for specific user.<br>
     * This token can be used for JWT Authentication.
     * 
     * @param id
     *            user ID
     * @param expire
     *            token expiration date
     * @return encrypted token
     * @throws JoseException
     */
    String generateAuthenticationToken(String id,
                                       Date expire)
        throws JoseException;

    /**
     * @param userName
     * @param token
     */
    void verifySecurityToken(String userName,
                             String token);

}
