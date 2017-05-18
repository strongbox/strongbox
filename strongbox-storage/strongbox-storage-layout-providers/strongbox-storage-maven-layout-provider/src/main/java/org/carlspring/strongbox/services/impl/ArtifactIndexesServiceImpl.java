package org.carlspring.strongbox.services.impl;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.locator.handlers.MavenIndexerManagementOperation;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.storage.StorageProvider;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Kate Novik.
 */
@Component("artifactIndexesService")
public class ArtifactIndexesServiceImpl
        implements ArtifactIndexesService
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactIndexesServiceImpl.class);

    @Inject
    private ConfigurationManager configurationManager;
    @Inject
    private RepositoryIndexManager repositoryIndexManager;
    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;
    @Inject
    private StorageProviderRegistry storageProviderRegistry;

    @Override
    public void rebuildIndex(String storageId,
                             String repositoryId,
                             String artifactPath)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        if (!repository.isIndexingEnabled())
        {
            return;
        }
        
        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());
        
        RepositoryPath repostitoryPath = layoutProvider.resolve(repository);
        if (artifactPath != null && artifactPath.trim().length() > 0)
        {
            repostitoryPath = repostitoryPath.resolve(artifactPath);
        }
        
        MavenIndexerManagementOperation operation = new MavenIndexerManagementOperation(repositoryIndexManager);

        operation.setStorage(storage);
        //noinspection ConstantConditions
        operation.setBasePath(repostitoryPath);

        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setOperation(operation);
        locator.locateArtifactDirectories();

        MavenRepositoryFeatures features = (MavenRepositoryFeatures) layoutProvider.getRepositoryFeatures();

        features.pack(storageId, repositoryId);
    }

    @Override
    public void rebuildIndexes(String storageId)
            throws IOException
    {
        Map<String, Repository> repositories = getRepositories(storageId);

        logger.debug("Rebuilding indexes for repositories " + repositories.keySet());

        for (String repository : repositories.keySet())
        {
            rebuildIndex(storageId, repository, null);
        }
    }

    @Override
    public void rebuildIndexes()
            throws IOException
    {
        Map<String, Storage> storages = getStorages();
        for (String storageId : storages.keySet())
        {
            rebuildIndexes(storageId);
        }
    }

    private Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    private Map<String, Storage> getStorages()
    {
        return getConfiguration().getStorages();
    }

    private Map<String, Repository> getRepositories(String storageId)
    {
        return getStorages().get(storageId)
                            .getRepositories();
    }

}
