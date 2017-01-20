package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.data.repository.OrientRepository;
import org.carlspring.strongbox.domain.ArtifactEntry;

import java.util.List;

import org.springframework.data.orient.commons.repository.annotation.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data CRUD repository for {@link ArtifactEntry}.
 *
 * @author Martin Todorov, Alex Oreshkevich
 */
@Transactional
public interface ArtifactRepository
        extends OrientRepository<ArtifactEntry>
{

    @Query("select * from ArtifactEntry where artifactCoordinates.artifactId = ?")
    List<ArtifactEntry> findByArtifactId(String artifactId);

    @Query("select * from ArtifactEntry where artifactCoordinates.groupId = ?")
    List<ArtifactEntry> findByGroupId(String groupId);

    @Query("select * from ArtifactEntry where artifactCoordinates.artifactId = ? and artifactCoordinates.groupId = ?")
    List<ArtifactEntry> findByArtifactIdAndGroupId(String artifactId,
                                                   String groupId);
}

