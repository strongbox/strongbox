package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationRepository;
import org.carlspring.strongbox.providers.AbstractMappedProviderRegistry;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class LayoutProviderRegistry extends AbstractMappedProviderRegistry<LayoutProvider>
{

    private static final Logger logger = LoggerFactory.getLogger(LayoutProviderRegistry.class);

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    protected ConfigurationRepository configurationRepository;


    public LayoutProviderRegistry()
    {
    }
    
    public void deleteTrash()
            throws IOException
    {
        for (Map.Entry entry : getConfiguration().getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            final Map<String, Repository> repositories = storage.getRepositories();
            for (Repository repository : repositories.values())
            {
                if (repository.allowsDeletion())
                {
                    logger.debug("Emptying trash for repository " + repository.getId() + "...");

                    final File basedirTrash = repository.getTrashDir();

                    FileUtils.deleteDirectory(basedirTrash);

                    //noinspection ResultOfMethodCallIgnored
                    basedirTrash.mkdirs();
                }
                else
                {
                    logger.warn("Repository " + repository.getId() + " does not support removal of trash.");
                }
            }
        }
    }

    public void undeleteTrash()
            throws ProviderImplementationException
    {
        for (Map.Entry entry : getConfiguration().getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            final Map<String, Repository> repositories = storage.getRepositories();
            for (Repository repository : repositories.values())
            {
                LayoutProvider layoutProvider = getLayoutProvider(repository, this);

                final String storageId = storage.getId();
                final String repositoryId = repository.getId();

                try
                {
                    if (repository.isTrashEnabled())
                    {
                        layoutProvider.undeleteTrash(storageId, repositoryId);
                    }
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Unable to undelete trash for storage " + storageId + " in repository " +
                                               repositoryId, e);
                }
            }
        }
    }

    @Override
    @PostConstruct
    public void initialize()
    {
        logger.info("Initialized the layout provider registry.");
    }

    @Override
    public Map<String, LayoutProvider> getProviders()
    {
        return super.getProviders();
    }

    @Override
    public void setProviders(Map<String, LayoutProvider> providers)
    {
        super.setProviders(providers);
    }

    @Override
    public LayoutProvider getProvider(String alias)
    {
        return super.getProvider(alias);
    }

    @Override
    public LayoutProvider addProvider(String alias, LayoutProvider provider)
    {
        LayoutProvider layoutProvider = super.addProvider(alias, provider);

        setRepositoryArtifactCoordinateValidators();

        return layoutProvider;
    }

    public void setRepositoryArtifactCoordinateValidators()
    {
        final Configuration configuration = getConfiguration();
        final Map<String, Storage> storages = configuration.getStorages();

        if (storages != null && !storages.isEmpty())
        {
            for (Storage storage : storages.values())
            {
                if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                {
                    for (Repository repository : storage.getRepositories().values())
                    {
                        LayoutProvider layoutProvider = getProvider(repository.getLayout());

                        // Generally, this should not happen. However, there are at least two cases where it may occur:
                        // 1) During testing -- various module are not added as dependencies and a layout provider
                        //    is thus not registered.
                        // 2) Syntax error, or some other mistake leading to an incorrectly defined layout
                        //    for a repository.
                        if (layoutProvider != null)
                        {
                            @SuppressWarnings("unchecked")
                            Set<String> defaultArtifactCoordinateValidators = layoutProvider.getDefaultArtifactCoordinateValidators();
                            if ((repository.getArtifactCoordinateValidators() == null ||
                                 (repository.getArtifactCoordinateValidators() != null &&
                                  repository.getArtifactCoordinateValidators().isEmpty())) &&
                                defaultArtifactCoordinateValidators != null)
                            {
                                logger.debug("");
                                logger.debug("Setting default artifact coordinate validators for " +
                                             repository.getStorage().getId() + ":" + repository.getId() + "...");
                                logger.debug("");

                                repository.setArtifactCoordinateValidators(defaultArtifactCoordinateValidators);
                            }
                        }
                    }
                }
            }
        }

        configurationRepository.updateConfiguration(configuration);
    }

    @Override
    public void removeProvider(String alias)
    {
        super.removeProvider(alias);
    }

    public static LayoutProvider getLayoutProvider(Repository repository,
                                                   LayoutProviderRegistry layoutProviderRegistry)
            throws ProviderImplementationException
    {
        return layoutProviderRegistry.getProvider(repository.getLayout());
    }

    public Configuration getConfiguration()
    {
        return configurationManagementService.getConfiguration();
    }

    public Storage getStorage(String storageId)
    {
        return configurationManagementService.getConfiguration()
                                             .getStorage(storageId);
    }

}
