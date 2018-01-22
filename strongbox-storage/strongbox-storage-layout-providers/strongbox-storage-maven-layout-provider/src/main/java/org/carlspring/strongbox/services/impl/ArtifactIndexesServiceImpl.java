package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.locator.handlers.MavenIndexerManagementOperation;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.repository.group.index.MavenIndexGroupRepositoryComponent;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.IndexContextHelper;

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
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private MavenRepositoryFeatures features;

    @Inject
    private MavenIndexGroupRepositoryComponent mavenIndexGroupRepositoryComponent;

    @Override
    public void addArtifactToIndex(RepositoryPath artifactPath)
            throws IOException
    {
        Repository repository = artifactPath.getFileSystem().getRepository();
        Storage storage = repository.getStorage();

        String contextId = IndexContextHelper.getContextId(storage.getId(),
                                                           repository.getId(),
                                                           IndexTypeEnum.LOCAL.getType());
        RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndexer(contextId);
        addArtifactToIndex(artifactPath, indexer);
    }

    @Override
    public void addArtifactToIndex(final RepositoryPath artifactPath,
                                   final RepositoryIndexer repositoryIndexer)
            throws IOException
    {
        if (repositoryIndexer == null)
        {
            return;
        }
        if (!features.isIndexingEnabled(getRepository(repositoryIndexer.getStorageId(),
                                                      repositoryIndexer.getRepositoryId())))
        {
            return;
        }

        repositoryIndexer.addArtifactToIndex(artifactPath);
    }

    @Override
    public void rebuildIndex(String storageId,
                             String repositoryId,
                             String artifactPath)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        if (!features.isIndexingEnabled(repository))
        {
            return;
        }
        if (repository.isGroupRepository())
        {
            mavenIndexGroupRepositoryComponent.rebuildIndex(repository, artifactPath);
        }
        else
        {
            MavenIndexerManagementOperation operation = new MavenIndexerManagementOperation(this);

            LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
            RepositoryPath repostitoryPath = layoutProvider.resolve(repository);
            if (artifactPath != null && artifactPath.trim().length() > 0)
            {
                repostitoryPath = repostitoryPath.resolve(artifactPath);
            }

            //noinspection ConstantConditions
            operation.setBasePath(repostitoryPath);

            ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
            locator.setOperation(operation);
            locator.locateArtifactDirectories();
        }

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
        return getStorages().get(storageId).getRepositories();
    }

    private Repository getRepository(String storageId,
                                     String repositoryId)
    {
        return getConfiguration().getStorage(storageId).getRepository(repositoryId);
    }

}
