package org.carlspring.strongbox.services;

import org.carlspring.strongbox.data.service.support.search.PagingCriteria;
import org.carlspring.strongbox.domain.RepositoryArtifactIdGroupEntry;

import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
public interface RepositoryArtifactIdGroupService
        extends ArtifactGroupService<RepositoryArtifactIdGroupEntry>
{

    long count(String storageId,
               String repositoryId);

    List<RepositoryArtifactIdGroupEntry> findMatching(String storageId,
                                                      String repositoryId,
                                                      PagingCriteria pagingCriteria);

    RepositoryArtifactIdGroupEntry findOneOrCreate(String storageId,
                                                   String repositoryId,
                                                   String artifactId);

    RepositoryArtifactIdGroupEntry findOne(String storageId,
                                           String repositoryId,
                                           String artifactId);

    @Override
    default Class<RepositoryArtifactIdGroupEntry> getEntityClass()
    {
        return RepositoryArtifactIdGroupEntry.class;
    }

}
