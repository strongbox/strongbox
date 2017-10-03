package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.client.ArtifactTransportException;

/**
 * @author carlspring
 */
public interface RepositoryFeatures
{

    // Supports indexing

    // Indexing implementation

    // List of supported checksums
    
    public void downloadRemoteIndex(String storageId,
                                    String repositoryId)
            throws ArtifactTransportException,
            RepositoryInitializationException;
}
