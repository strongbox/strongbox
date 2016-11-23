package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.data.repository.OrientRepository;
import org.carlspring.strongbox.domain.ArtifactEntry;

import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data CRUD repository for {@link ArtifactEntry}.
 *
 * @author Martin Todorov
 */
@Transactional
public interface ArtifactRepository
        extends OrientRepository<ArtifactEntry>
{

    // select * from User where username = 'admin'
    ArtifactEntry findByCoordinates(String coordinates);

}

