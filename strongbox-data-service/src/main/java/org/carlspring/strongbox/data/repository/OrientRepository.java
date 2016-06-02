package org.carlspring.strongbox.data.repository;

import org.springframework.data.orient.object.repository.OrientObjectRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Marker interface for defining CRUD API under any Object based on OrientDB storage.
 *
 * @author Alex Oreshkevich
 */
@NoRepositoryBean
public interface OrientRepository<T>
        extends OrientObjectRepository<T>
{

}
