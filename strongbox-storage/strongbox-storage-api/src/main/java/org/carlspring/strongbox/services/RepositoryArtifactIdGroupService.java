package org.carlspring.strongbox.services;

import org.carlspring.strongbox.domain.RepositoryArtifactIdGroup;

/**
 * @author Przemyslaw Fusik
 */
public interface RepositoryArtifactIdGroupService
        extends ArtifactGroupService<RepositoryArtifactIdGroup>
{

    @Override
    default Class<RepositoryArtifactIdGroup> getEntityClass()
    {
        return RepositoryArtifactIdGroup.class;
    }

    RepositoryArtifactIdGroup findOneOrCreate(String storageId,
                                              String repositoryId,
                                              String id);
}
