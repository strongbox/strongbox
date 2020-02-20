package org.carlspring.strongbox.services;

import org.carlspring.strongbox.data.service.support.search.PagingCriteria;
import org.carlspring.strongbox.domain.RepositoryArtifactIdGroupEntity;

import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
public interface RepositoryArtifactIdGroupService
        extends ArtifactGroupService<RepositoryArtifactIdGroupEntity>
{

    long count(String storageId,
               String repositoryId);

    List<RepositoryArtifactIdGroupEntity> findMatching(String storageId,
                                                      String repositoryId,
                                                      PagingCriteria pagingCriteria);

    RepositoryArtifactIdGroupEntity findOneOrCreate(String storageId,
                                                   String repositoryId,
                                                   String artifactId);

    RepositoryArtifactIdGroupEntity findOne(String storageId,
                                           String repositoryId,
                                           String artifactId);

    @Override
    default Class<RepositoryArtifactIdGroupEntity> getEntityClass()
    {
        return RepositoryArtifactIdGroupEntity.class;
    }

}
