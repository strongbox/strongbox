package org.carlspring.strongbox.data.service;

import org.carlspring.strongbox.data.domain.StrongboxUser;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Alex Oreshkevich
 */
@Transactional
public interface StrongboxUserService
        extends CrudService<StrongboxUser, String>
{

    @Transactional
    Optional<StrongboxUser> findByUserName(final String username);
}
