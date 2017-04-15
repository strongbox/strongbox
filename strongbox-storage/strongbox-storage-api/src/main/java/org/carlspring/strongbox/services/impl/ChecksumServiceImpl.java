package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.artifact.locator.handlers.ArtifactLocationGenerateChecksumOperation;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.storage.StorageProvider;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.services.ChecksumService;
import org.carlspring.strongbox.storage.Storage;
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
    private LayoutProviderRegistry layoutProviderRegistry;
    @Inject
    private StorageProviderRegistry storageProviderRegistryl;

    @Override
    public void regenerateChecksum(String storageId,
                                   String repositoryId,
                                   String basePath,
                                   boolean forceRegeneration)
        throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        
        StorageProvider storageProvider = storageProviderRegistryl.getProvider(repository.getImplementation());
        RepositoryPath repositoryBasePath = storageProvider.resolve(repository, basePath);
        
        ArtifactLocationGenerateChecksumOperation operation = new ArtifactLocationGenerateChecksumOperation(
                layoutProviderRegistry);
        operation.setStorage(storage);
        operation.setBasePath(repositoryBasePath);
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
