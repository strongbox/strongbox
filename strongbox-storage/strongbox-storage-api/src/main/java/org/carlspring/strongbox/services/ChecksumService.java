package org.carlspring.strongbox.services;

import java.io.IOException;

/**
 * @author Kate Novik.
 */
public interface ChecksumService
{

    /**
     * Regenerate checksum for artifact using artifactPath (string)
     *
     * @param storageId         String
     * @param repositoryId      String
     * @param artifactPath      String
     * @param forceRegeneration boolean
     */
    void regenerateChecksum(String storageId,
                            String repositoryId,
                            String artifactPath,
                            boolean forceRegeneration)
            throws IOException;

}
