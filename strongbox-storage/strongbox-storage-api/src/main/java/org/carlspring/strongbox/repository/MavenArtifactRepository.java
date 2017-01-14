package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.data.repository.OrientRepository;
import org.carlspring.strongbox.domain.ArtifactEntry;

import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data CRUD repository for {@link Mavenar}.
 *
 * @author Martin Todorov
 */
@Transactional
public interface MavenArtifactRepository
        extends OrientRepository<ArtifactEntry>
{

    //ArtifactEntry findByCoordinates(String coordinates);
}

