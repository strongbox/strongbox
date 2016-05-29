package org.carlspring.strongbox.data.service;

import org.carlspring.strongbox.data.domain.StrongboxUser;

import java.util.Optional;

/**
 * @author Alex Oreshkevich
 */
public interface StrongboxUserService extends CrudService<StrongboxUser, String> {

    Optional<StrongboxUser> findByUserName(final String username);
}
