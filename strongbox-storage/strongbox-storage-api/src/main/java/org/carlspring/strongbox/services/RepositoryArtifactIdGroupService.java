package org.carlspring.strongbox.services;

import org.carlspring.strongbox.domain.RepositoryArtifactIdGroup;

/**
 * @author Przemyslaw Fusik
 */
public interface RepositoryArtifactIdGroupService
        extends ArtifactGroupService<RepositoryArtifactIdGroup>
{

    RepositoryArtifactIdGroup findOneOrCreate(String storageId,
                                              String repositoryId,
                                              String artifactId);
    
    @Override
    default Class<RepositoryArtifactIdGroup> getEntityClass()
    {
        return RepositoryArtifactIdGroup.class;
    }

}
