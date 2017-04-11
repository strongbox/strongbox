package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.locator.handlers.MavenIndexerManagementOperation;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

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
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;


    @Override
    public void rebuildIndex(String storageId,
                             String repositoryId,
                             String artifactPath)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        artifactPath = artifactPath == null ? "/" : artifactPath;

        if (repository.isIndexingEnabled())
        {
            MavenIndexerManagementOperation operation = new MavenIndexerManagementOperation(repositoryIndexManager);

            operation.setStorage(storage);
            operation.setRepository(repository);
            //noinspection ConstantConditions
            operation.setBasePath(artifactPath);

            ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
            locator.setOperation(operation);
            locator.locateArtifactDirectories();

            LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
            MavenRepositoryFeatures features = (MavenRepositoryFeatures) layoutProvider.getRepositoryFeatures();

            features.pack(storageId, repositoryId);
        }
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
        return getStorages().get(storageId).getRepositories();
    }

}
