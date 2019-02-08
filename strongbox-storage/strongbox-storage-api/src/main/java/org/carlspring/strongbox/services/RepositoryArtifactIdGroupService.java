package org.carlspring.strongbox.services;

import org.carlspring.strongbox.domain.RepositoryArtifactIdGroupEntry;

/**
 * @author Przemyslaw Fusik
 */
public interface RepositoryArtifactIdGroupService
        extends ArtifactGroupService<RepositoryArtifactIdGroupEntry>
{

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
