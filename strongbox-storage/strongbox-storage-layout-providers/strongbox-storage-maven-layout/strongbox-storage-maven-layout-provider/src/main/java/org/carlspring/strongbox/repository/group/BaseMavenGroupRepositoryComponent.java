package org.carlspring.strongbox.repository.group;

import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
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
import org.apache.commons.collections4.CollectionUtils;
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
    
    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    public void cleanupGroupsContaining(RepositoryPath repositoryPath)
            throws IOException
    {
        cleanupGroupsContaining(repositoryPath, new HashMap<>());
    }

    private void cleanupGroupsContaining(RepositoryPath repositoryPath,
                                         final Map<String, MutableBoolean> repositoryArtifactExistence)
            throws IOException
    {
        Repository repository = repositoryPath.getRepository();
        final List<Repository> directParents = configurationManagementService.getConfiguration()
                                                                             .getGroupRepositoriesContaining(repository.getStorage().getId(),
                                                                                                                      repository.getId());
        if (CollectionUtils.isEmpty(directParents))
        {
            return;
        }
        
        String artifactPath = RepositoryFiles.relativizePath(repositoryPath);
        
        for (final Repository groupRepository : directParents)
        {

            boolean artifactExists = groupRepositoryArtifactExistenceChecker.artifactExistsInTheGroupRepositorySubTree(groupRepository,
                                                                                                                       repositoryPath,
                                                                                                                       repositoryArtifactExistence);

            if (!artifactExists)
            {
                cleanupGroupWhenArtifactPathNoLongerExistsInSubTree(groupRepository, artifactPath);
            }
            
            cleanupGroupsContaining(repositoryPathResolver.resolve(groupRepository, repositoryPath),
                                    repositoryArtifactExistence);
        }
    }

    protected abstract void cleanupGroupWhenArtifactPathNoLongerExistsInSubTree(Repository groupRepository,
                                                                                String artifactPath)
            throws IOException;


    public void updateGroupsContaining(RepositoryPath repositoryPath)
            throws IOException
    {

        final UpdateCallback updateCallback = newInstance(repositoryPath);
        try
        {
            updateCallback.beforeUpdate();
        }
        catch (StopUpdateSilentlyException ex)
        {
            return;
        }


        Repository repository = repositoryPath.getRepository();
        updateGroupsContaining(repositoryPath, Lists.newArrayList(repository), updateCallback);
    }

    private void updateGroupsContaining(final RepositoryPath repositoryPath,
                                        final List<Repository> leafRoute,
                                        final UpdateCallback updateCallback)
            throws IOException
    {
        Repository repository = repositoryPath.getRepository();
        final List<Repository> groupRepositories = configurationManagementService.getConfiguration()
                                                                                 .getGroupRepositoriesContaining(repository.getStorage().getId(),
                                                                                                                 repository.getId());
        if (CollectionUtils.isEmpty(groupRepositories))
        {
            return;
        }
        String artifactPath = RepositoryFiles.relativizePath(repositoryPath);
        for (final Repository parent : groupRepositories)
        {
            RepositoryPath parentRepositoryArtifactAbsolutePath = repositoryPathResolver.resolve(parent, repositoryPath);
            
            if (!isOperationDeniedByRoutingRules(parent, leafRoute, artifactPath))
            {
                updateCallback.performUpdate(parentRepositoryArtifactAbsolutePath);
            }

            leafRoute.add(parent);

            updateGroupsContaining(parentRepositoryArtifactAbsolutePath, leafRoute, updateCallback);

            leafRoute.remove(parent);
        }
    }

    protected RepositoryPath getRepositoryPath(final Repository repository)
    {
        return repositoryPathResolver.resolve(repository);
    }

    protected LayoutProvider getRepositoryProvider(final Repository repository)
    {
        return layoutProviderRegistry.getProvider(repository.getLayout());
    }

    protected boolean isOperationDeniedByRoutingRules(final Repository groupRepository,
                                                      final List<Repository> leafRoute,
                                                      final String artifactPath) throws IOException
    {
        for (final Repository leaf : leafRoute)
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(leaf).resolve(artifactPath);
            if (artifactRoutingRulesChecker.isDenied(groupRepository, repositoryPath))
            {
                return true;
            }
        }
        return false;
    }

    protected abstract UpdateCallback newInstance(RepositoryPath repositoryPath);

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
