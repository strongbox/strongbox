package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.client.ArtifactTransportException;

/**
 * @author carlspring
 */
public class P2RepositoryFeatures
        implements RepositoryFeatures
{

    @Override
    public void downloadRemoteIndex(String storageId,
                                    String repositoryId)
        throws ArtifactTransportException,
        RepositoryInitializationException
    {
        
    }

}
