package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.data.repository.OrientRepository;
import org.carlspring.strongbox.domain.ArtifactEntry;

import java.util.List;

import org.springframework.data.orient.commons.repository.annotation.Query;
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

    @Query("select * from ArtifactEntry where artifactCoordinates.groupId = ?")
        // "select * from ArtifactEntry where artifactCoordinates.groupId = 'org.carlspring.strongbox'"
    List<ArtifactEntry> findByArtifactCoordinates(String groupId);
}

