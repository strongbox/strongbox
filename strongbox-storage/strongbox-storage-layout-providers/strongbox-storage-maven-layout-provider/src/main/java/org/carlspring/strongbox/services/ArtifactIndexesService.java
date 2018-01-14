package org.carlspring.strongbox.services;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;

import java.io.IOException;

/**
 * @author Kate Novik.
 */
public interface ArtifactIndexesService
{

    void addArtifactToIndex(RepositoryPath artifactPath)
            throws IOException;

    /**
     * Alternative method to {@link ArtifactIndexesService#addArtifactToIndex(RepositoryPath)}
     * which gives additional ability to specify another repositoryIndexer to store the underlying artifact
     */
    void addArtifactToIndex(RepositoryPath artifactPath, RepositoryIndexer repositoryIndexer)
            throws IOException;

    /**
     * Rebuild indexes for artifact using artifactPath (string)
     * or for all artifacts in repository, when artifactPath is null
     *
     * @param storageId    String
     * @param repositoryId String
     * @param artifactPath String
     */
    void rebuildIndex(String storageId,
                      String repositoryId,
                      String artifactPath)
            throws IOException;

    /**
     * Rebuild indexes for all artifacts in storage
     *
     * @param storageId String
     */
    void rebuildIndexes(String storageId)
            throws IOException;

    /**
     * Rebuild indexes for artifacts in all storages
     */
    void rebuildIndexes()
            throws IOException;

}
