package org.carlspring.strongbox.repository.group.index;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.config.MavenIndexerEnabledCondition;
import org.carlspring.strongbox.locator.handlers.MavenGroupRepositoryIndexerManagementOperation;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.IndexedMaven2FileSystemProvider;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponent;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.IndexContextHelper;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
@Conditional(MavenIndexerEnabledCondition.class)
public class MavenIndexGroupRepositoryComponent
        extends BaseMavenGroupRepositoryComponent
{

    @Inject
    private ArtifactIndexesService artifactIndexesService;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    public void rebuildIndex(final Repository groupRepository)
            throws IOException
    {
        rebuildIndex(groupRepository, null);
    }

    public void rebuildIndex(final Repository groupRepository,
                             final String artifactPath)
            throws IOException
    {
        final Set<Repository> traversedSubRepositories = groupRepositorySetCollector.collect(groupRepository, true);
        for (final Repository subRepository : traversedSubRepositories)
        {
            if (!subRepository.isGroupRepository())
            {
                final MavenGroupRepositoryIndexerManagementOperation operation = new MavenGroupRepositoryIndexerManagementOperation(artifactIndexesService,
                                                                                                                                    repositoryIndexManager,
                                                                                                                                    groupRepository);
                RepositoryPath basePath = getRepositoryPath(subRepository);
                basePath = StringUtils.isEmpty(artifactPath) ? basePath : basePath.resolve(basePath);
                if (!Files.exists(basePath))
                {
                    continue;
                }
                operation.setBasePath(basePath);
                final ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
                locator.setOperation(operation);
                locator.locateArtifactDirectories();
            }
        }
    }

    @Override
    protected void cleanupGroupWhenArtifactPathNoLongerExistsInSubTree(Repository groupRepository,
                                                                       String artifactPath)
            throws IOException
    {
        LayoutProvider layoutProvider = getRepositoryProvider(groupRepository);
        if (!(layoutProvider instanceof Maven2LayoutProvider))
        {
            logger.error(
                    "Layout provider {} associated with the group repository {} is not supported here. Related path is {}",
                    groupRepository.getId(), layoutProvider, artifactPath);
            return;
        }
        
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(groupRepository).resolve(artifactPath);
        IndexedMaven2FileSystemProvider provider = (IndexedMaven2FileSystemProvider) repositoryPath.getFileSystem().provider();
        
        provider.deleteFromIndex(repositoryPath);
    }

    @Override
    protected UpdateCallback newInstance(RepositoryPath repositoryPath)
    {
        return new IndexUpdateCallback(repositoryPath);
    }

    class IndexUpdateCallback
            implements UpdateCallback
    {

        private final RepositoryPath initiatorRepositoryPath;

        IndexUpdateCallback(RepositoryPath repositoryPath)
        {
            this.initiatorRepositoryPath = repositoryPath;
        }

        @Override
        public void performUpdate(final RepositoryPath parentRepositoryArtifactAbsolutePath)
                throws IOException
        {
            final RepositoryPath artifactAbsolutePath = initiatorRepositoryPath.toAbsolutePath();

            final Repository parent = parentRepositoryArtifactAbsolutePath.getFileSystem().getRepository();
            final String contextId = IndexContextHelper.getContextId(parent.getStorage().getId(), parent.getId(),
                                                                     IndexTypeEnum.LOCAL.getType());
            final RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndexer(contextId);

            artifactIndexesService.addArtifactToIndex(artifactAbsolutePath, indexer);
        }
    }

}
