package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.storage.repository.Repository;

import java.util.Set;

/**
 * @author carlspring
 */
public interface RepositoryFeatures
{

    /**
     * Used to get a new pre-configured instance of a Repository object for the given layout provider.
     *
     * @return
     */
    Repository createRepositoryInstance(String storageId, String repositoryId);

    /**
     * Returns the default list of artifact coordinata validators.
     *
     * @return
     */
    Set<String> getDefaultArtifactCoordinateValidators();

}
