package org.carlspring.strongbox.repository.group;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.repository.group.GroupRepositoryArtifactExistenceChecker;
import org.carlspring.strongbox.providers.repository.group.GroupRepositorySetCollector;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.support.ArtifactRoutingRulesChecker;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Przemyslaw Fusik
 */
public abstract class BaseMavenGroupRepositoryComponent
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    protected LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    protected GroupRepositorySetCollector groupRepositorySetCollector;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private GroupRepositoryArtifactExistenceChecker groupRepositoryArtifactExistenceChecker;

    @Inject
    private ArtifactRoutingRulesChecker artifactRoutingRulesChecker;

    public void cleanupGroupsContaining(final String storageId,
                                        final String repositoryId,
                                        final String artifactPath)
            throws IOException
    {
        cleanupGroupsContaining(storageId, repositoryId, artifactPath, new HashMap<>());
    }

    private void cleanupGroupsContaining(final String storageId,
                                         final String repositoryId,
                                         final String artifactPath,
                                         final Map<String, MutableBoolean> repositoryArtifactExistence)
            throws IOException
    {
        final List<Repository> directParents = configurationManagementService.getGroupRepositoriesContaining(storageId,
                                                                                                             repositoryId);
        if (CollectionUtils.isEmpty(directParents))
        {
            return;
        }
        for (final Repository groupRepository : directParents)
        {

            boolean artifactExists = groupRepositoryArtifactExistenceChecker.artifactExistsInTheGroupRepositorySubTree(
                    groupRepository,
                    artifactPath,
                    repositoryArtifactExistence);

            if (!artifactExists)
            {
                cleanupGroupWhenArtifactPathNoLongerExistsInSubTree(groupRepository, artifactPath);
            }

            cleanupGroupsContaining(groupRepository.getStorage().getId(),
                                    groupRepository.getId(),
                                    artifactPath,
                                    repositoryArtifactExistence);
        }
    }

    protected abstract void cleanupGroupWhenArtifactPathNoLongerExistsInSubTree(Repository groupRepository,
                                                                                String artifactPath)
            throws IOException;


    public void updateGroupsContaining(final String storageId,
                                       final String repositoryId,
                                       final String artifactPath)
            throws IOException
    {

        final UpdateCallback updateCallback = newInstance(storageId, repositoryId, artifactPath);
        try
        {
            updateCallback.beforeUpdate();
        }
        catch (StopUpdateSilentlyException ex)
        {
            return;
        }

        final Repository repository = getRepository(storageId, repositoryId);

        updateGroupsContaining(repository, artifactPath, Lists.newArrayList(repository), updateCallback);
    }

    private void updateGroupsContaining(final Repository repository,
                                        final String artifactPath,
                                        final List<Repository> leafRoute,
                                        final UpdateCallback updateCallback)
            throws IOException
    {
        final List<Repository> groupRepositories = configurationManagementService.getGroupRepositoriesContaining(
                repository.getStorage().getId(), repository.getId());
        if (CollectionUtils.isEmpty(groupRepositories))
        {
            return;
        }
        for (final Repository parent : groupRepositories)
        {
            if (!isOperationDeniedByRoutingRules(parent, leafRoute, artifactPath))
            {
                final RepositoryPath parentRepositoryAbsolutePath = getRepositoryPath(parent);
                final RepositoryPath parentRepositoryArtifactAbsolutePath = parentRepositoryAbsolutePath.resolve(
                        artifactPath);

                updateCallback.performUpdate(parentRepositoryArtifactAbsolutePath);
            }

            leafRoute.add(parent);

            updateGroupsContaining(parent, artifactPath, leafRoute, updateCallback);

            leafRoute.remove(parent);
        }
    }

    protected RepositoryPath getRepositoryPath(final Repository repository)
    {
        final LayoutProvider layoutProvider = getRepositoryProvider(repository);
        return layoutProvider.resolve(repository);
    }

    protected LayoutProvider getRepositoryProvider(final Repository repository)
    {
        return layoutProviderRegistry.getProvider(repository.getLayout());
    }

    protected boolean isOperationDeniedByRoutingRules(final Repository groupRepository,
                                                      final List<Repository> leafRoute,
                                                      final String artifactPath)
    {
        for (final Repository leaf : leafRoute)
        {
            if (artifactRoutingRulesChecker.isDenied(groupRepository.getId(), leaf.getId(), artifactPath))
            {
                return true;
            }
        }
        return false;
    }

    protected abstract UpdateCallback newInstance(final String storageId,
                                                  final String repositoryId,
                                                  final String artifactPath);

    protected Repository getRepository(final String storageId,
                                       final String repositoryId)
    {
        return configurationManagementService.getConfiguration()
                                             .getStorage(storageId)
                                             .getRepository(repositoryId);
    }

    protected interface UpdateCallback
    {

        default void beforeUpdate()
                throws IOException
        {
            // do nothing, by default
        }

        default void performUpdate(RepositoryPath parentRepositoryArtifactAbsolutePath)
                throws IOException
        {
            // do nothing, by default
        }
    }

    public static class StopUpdateSilentlyException
            extends RuntimeException
    {


    }
}
