package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.artifact.locator.handlers.ArtifactLocationGenerateChecksumOperation;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ChecksumService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Kate Novik.
 */
@Component
public class ChecksumServiceImpl
        implements ChecksumService
{
    private final Logger logger = LoggerFactory.getLogger(ChecksumServiceImpl.class);

    @Inject
    private ConfigurationManager configurationManager;
    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Override
    public void regenerateChecksum(String storageId,
                                   String repositoryId,
                                   String basePath,
                                   boolean forceRegeneration)
        throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        if (layoutProvider == null)
        {
            logger.warn("Trying to regenerate checksum for repository {} but layoutProvider was not found in registry {} ",
                        repository.getId(), repository.getLayout());
            return;
        }
        RepositoryPath repositoryBasePath = layoutProvider.resolve(repository).resolve(basePath);
        
        ArtifactLocationGenerateChecksumOperation operation = new ArtifactLocationGenerateChecksumOperation();
        operation.setBasePath(repositoryBasePath);
        operation.setForceRegeneration(forceRegeneration);

        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setOperation(operation);
        locator.locateArtifactDirectories();
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }
}
