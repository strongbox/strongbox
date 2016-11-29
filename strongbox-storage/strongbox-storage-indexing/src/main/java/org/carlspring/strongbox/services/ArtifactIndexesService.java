package org.carlspring.strongbox.services;

import java.io.IOException;

/**
 * @author Kate Novik.
 */
public interface ArtifactIndexesService
{

    /**
     * Rebuild indexes for artifact using artifactPath (string)
     *
     * @param storageId    String
     * @param repositoryId String
     * @param artifactPath String
     */
    void rebuildIndexes(String storageId,
                        String repositoryId,
                        String artifactPath)
            throws IOException;

}
