package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.ConfigurationUtils;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.repository.GroupRepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.providers.repository.event.GroupRepositoryPathFetchEvent;
import org.carlspring.strongbox.services.support.ArtifactRoutingRulesChecker;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenGroupRepositoryPathFetchEventListener
{

    private static final Logger logger = LoggerFactory.getLogger(MavenGroupRepositoryPathFetchEventListener.class);

    @Inject
    private Maven2LayoutProvider maven2LayoutProvider;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    private ArtifactRoutingRulesChecker artifactRoutingRulesChecker;

    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @EventListener
    public void handle(final GroupRepositoryPathFetchEvent event)
            throws IOException
    {
        RepositoryPath repositoryPath = event.getPath();
        if (!Maven2LayoutProvider.ALIAS.equals(repositoryPath.getRepository().getLayout()))
        {
            return;
        }

        if (!maven2LayoutProvider.requiresGroupAggregation(repositoryPath))
        {
            return;
        }

        fetchInSubRepositories(repositoryPath);
    }

    /**
     * @see GroupRepositoryProvider#resolvePathTraversal(org.carlspring.strongbox.providers.io.RepositoryPath)
     */
    private void fetchInSubRepositories(final RepositoryPath repositoryPath)
            throws IOException
    {
        Repository groupRepository = repositoryPath.getRepository();
        Storage storage = groupRepository.getStorage();
        List<Callable<Path>> fetchActions = new ArrayList<>();

        for (String storageAndRepositoryId : groupRepository.getGroupRepositories())
        {
            String sId = ConfigurationUtils.getStorageId(storage.getId(), storageAndRepositoryId);
            String rId = ConfigurationUtils.getRepositoryId(storageAndRepositoryId);
            Repository subRepository = configurationManager.getRepository(sId, rId);

            if (!subRepository.isInService())
            {
                continue;
            }

            RepositoryPath resolvedPath = repositoryPathResolver.resolve(subRepository, repositoryPath);
            if (artifactRoutingRulesChecker.isDenied(groupRepository, resolvedPath))
            {
                continue;
            }

            RepositoryProvider provider = repositoryProviderRegistry.getProvider(subRepository.getType());
            fetchActions.add(() -> provider.fetchPath(resolvedPath));
        }

        fetchPathsInParallel(fetchActions);
    }

    private void fetchPathsInParallel(final List<Callable<Path>> fetchActions)
    {
        fetchActions
                .parallelStream()
                .forEach(action -> {
                    try
                    {
                        action.call();
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage(), e);
                    }
                });
    }
}
