package org.carlspring.strongbox.data.repository;

import org.carlspring.strongbox.data.domain.StrongboxUser;

/**
 * Spring Data CRUD repository for {@link StrongboxUser}.
 *
 * @author Alex Oreshkevich
 */
public interface StrongboxUserRepository
        extends OrientRepository<StrongboxUser>
{

    // select * from StrongboxUser where username = 'admin'
    StrongboxUser findByUsername(String userName);
}
