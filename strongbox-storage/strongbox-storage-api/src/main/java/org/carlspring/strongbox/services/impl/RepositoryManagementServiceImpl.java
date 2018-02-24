package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.event.repository.RepositoryEvent;
import org.carlspring.strongbox.event.repository.RepositoryEventListenerRegistry;
import org.carlspring.strongbox.event.repository.RepositoryEventTypeEnum;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;
import org.carlspring.strongbox.storage.validation.resource.ArtifactOperationsValidator;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("repositoryManagementService")
public class RepositoryManagementServiceImpl
        implements RepositoryManagementService
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryManagementServiceImpl.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private ArtifactOperationsValidator artifactOperationsValidator;

    @Inject
    private RepositoryEventListenerRegistry repositoryEventListenerRegistry;


    @Override
    public void createRepository(String storageId,
                                 String repositoryId)
            throws IOException, RepositoryManagementStrategyException
    {
        LayoutProvider provider = getLayoutProvider(storageId, repositoryId);
        if (provider != null)
        {
            provider.getRepositoryManagementStrategy().createRepository(storageId, repositoryId);
        }
        else
        {
            Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

            logger.warn("Layout provider '" + repository.getLayout() + "' could not be resolved. " +
                        "Using generic implementation instead.");

            File repositoryDir = new File(repository.getStorage().getBasedir(), repositoryId);
            if (!repositoryDir.exists())
            {
                //noinspection ResultOfMethodCallIgnored
                repositoryDir.mkdirs();
            }
        }

        RepositoryEvent event = new RepositoryEvent(storageId,
                                                    repositoryId,
                                                    RepositoryEventTypeEnum.EVENT_REPOSITORY_CREATED.getType());

        repositoryEventListenerRegistry.dispatchEvent(event);
    }

    @Override
    public void removeRepository(String storageId,
                                 String repositoryId)
            throws IOException
    {
        LayoutProvider provider = getLayoutProvider(storageId, repositoryId);
        provider.getRepositoryManagementStrategy().removeRepository(storageId, repositoryId);

        RepositoryEvent event = new RepositoryEvent(storageId,
                                                    repositoryId,
                                                    RepositoryEventTypeEnum.EVENT_REPOSITORY_DELETED.getType());

        repositoryEventListenerRegistry.dispatchEvent(event);
    }

    @Override
    public void deleteTrash(String storageId, String repositoryId)
            throws IOException
    {
        artifactOperationsValidator.checkStorageExists(storageId);
        artifactOperationsValidator.checkRepositoryExists(storageId, repositoryId);

        try
        {
            final Storage storage = getStorage(storageId);
            final Repository repository = storage.getRepository(repositoryId);

            artifactOperationsValidator.checkAllowsDeletion(repository);

            LayoutProvider layoutProvider = getLayoutProvider(storageId, repositoryId);
            layoutProvider.deleteTrash(storageId, repositoryId);

            RepositoryEvent event = new RepositoryEvent(storageId,
                                                        repositoryId,
                                                        RepositoryEventTypeEnum.EVENT_REPOSITORY_EMTPY_TRASH.getType());

            repositoryEventListenerRegistry.dispatchEvent(event);
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteTrash()
            throws ArtifactStorageException
    {
        try
        {
            layoutProviderRegistry.deleteTrash();

            int type = RepositoryEventTypeEnum.EVENT_REPOSITORY_EMTPY_TRASH_FOR_ALL_REPOSITORIES.getType();
            RepositoryEvent event = new RepositoryEvent(null, null, type);

            repositoryEventListenerRegistry.dispatchEvent(event);
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void undelete(String storageId, String repositoryId, String artifactPath)
            throws IOException
    {
        artifactOperationsValidator.validate(storageId, repositoryId, artifactPath);

        final Storage storage = getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);

        artifactOperationsValidator.checkAllowsDeletion(repository);

        try
        {
            LayoutProvider layoutProvider = getLayoutProvider(storageId, repositoryId);
            layoutProvider.undelete(storageId, repositoryId, artifactPath);

            int type = RepositoryEventTypeEnum.EVENT_REPOSITORY_EMTPY_TRASH_FOR_ALL_REPOSITORIES.getType();
            RepositoryEvent event = new RepositoryEvent(storageId,
                                                        repositoryId,
                                                        artifactPath,
                                                        type);

            repositoryEventListenerRegistry.dispatchEvent(event);
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void undeleteTrash(String storageId, String repositoryId)
            throws IOException
    {
        artifactOperationsValidator.checkStorageExists(storageId);
        artifactOperationsValidator.checkRepositoryExists(storageId, repositoryId);

        try
        {
            final Storage storage = getStorage(storageId);
            final Repository repository = storage.getRepository(repositoryId);

            if (repository.isTrashEnabled())
            {
                LayoutProvider layoutProvider = getLayoutProvider(storageId, repositoryId);
                layoutProvider.undeleteTrash(storageId, repositoryId);

                RepositoryEvent event = new RepositoryEvent(storageId,
                                                            repositoryId,
                                                            null,
                                                            RepositoryEventTypeEnum.EVENT_REPOSITORY_UNDELETE_TRASH
                                                                                   .getType());

                repositoryEventListenerRegistry.dispatchEvent(event);
            }
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void undeleteTrash()
            throws ProviderImplementationException
    {
        layoutProviderRegistry.undeleteTrash();
        RepositoryEvent event = new RepositoryEvent(null,
                                                    null,
                                                    null,
                                                    RepositoryEventTypeEnum.EVENT_REPOSITORY_UNDELETE_TRASH_FOR_ALL_REPOSITORIES
                                                                           .getType());
        repositoryEventListenerRegistry.dispatchEvent(event);
    }

    @Override
    public void putInService(String storageId,
                             String repositoryId)
    {
        getConfiguration().getStorage(storageId)
                          .getRepository(repositoryId)
                          .setStatus(RepositoryStatusEnum.IN_SERVICE.getStatus());

        RepositoryEvent event = new RepositoryEvent(storageId,
                                                    repositoryId,
                                                    RepositoryEventTypeEnum.EVENT_REPOSITORY_PUT_IN_SERVICE.getType());

        repositoryEventListenerRegistry.dispatchEvent(event);
    }

    @Override
    public void putOutOfService(String storageId,
                                String repositoryId)
    {
        getConfiguration().getStorage(storageId)
                          .getRepository(repositoryId)
                          .setStatus(RepositoryStatusEnum.OUT_OF_SERVICE.getStatus());

        RepositoryEvent event = new RepositoryEvent(storageId,
                                                    repositoryId,
                                                    RepositoryEventTypeEnum.EVENT_REPOSITORY_PUT_OUT_OF_SERVICE
                                                                           .getType());

        repositoryEventListenerRegistry.dispatchEvent(event);
    }

    private LayoutProvider getLayoutProvider(String storageId,
                                             String repositoryId)
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        return layoutProviderRegistry.getProvider(repository.getLayout());
    }

    @Override
    public Storage getStorage(String storageId)
    {
        return getConfiguration().getStorages().get(storageId);
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
