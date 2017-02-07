package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.artifact.locator.handlers.ArtifactLocationGenerateMavenChecksumOperation;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.ChecksumService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.checksum.MavenChecksumManager;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * @author Kate Novik.
 */
@Component
public class ChecksumServiceImpl
        implements ChecksumService
{

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private MavenChecksumManager mavenChecksumManager;

    @Override
    public void regenerateChecksum(String storageId,
                                   String repositoryId,
                                   String basePath,
                                   boolean forceRegeneration)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        ArtifactLocationGenerateMavenChecksumOperation operation = new ArtifactLocationGenerateMavenChecksumOperation(mavenChecksumManager);
        operation.setStorage(storage);
        operation.setRepository(repository);
        operation.setBasePath(basePath);
        operation.setForceRegeneration(forceRegeneration);

        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setOperation(operation);
        locator.locateArtifactDirectories();
    }

    @Override
    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }
}
