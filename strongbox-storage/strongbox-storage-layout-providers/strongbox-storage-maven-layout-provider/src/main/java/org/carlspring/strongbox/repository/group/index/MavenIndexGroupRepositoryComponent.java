package org.carlspring.strongbox.repository.group.index;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.locator.handlers.MavenGroupRepositoryIndexerManagementOperation;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponent;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenIndexGroupRepositoryComponent
        extends BaseMavenGroupRepositoryComponent
{

    @Inject
    private ArtifactIndexesService artifactIndexesService;

    public void initialize(final Repository groupRepository)
            throws IOException
    {
        final RepositoryPath groupRepositoryPath = getRepositoryPath(groupRepository);

        final Set<Repository> traversedSubRepositories = groupRepositorySetCollector.collect(groupRepository, true);
        for (final Repository subRepository : traversedSubRepositories)
        {
            if (!subRepository.isGroupRepository())
            {
                final MavenGroupRepositoryIndexerManagementOperation operation = new MavenGroupRepositoryIndexerManagementOperation(artifactIndexesService,
                                                                                                                                    groupRepositoryPath);
                operation.setBasePath(getRepositoryPath(subRepository));
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
        final LayoutProvider layoutProvider = getRepositoryProvider(groupRepository);
        if (!(layoutProvider instanceof Maven2LayoutProvider))
        {
            logger.error(
                    "Layout provider {} associated with the group repository {} is not supported here. Related path is {}",
                    groupRepository.getId(), layoutProvider, artifactPath);
            return;
        }
        final RepositoryPath repositoryPath = layoutProvider.resolve(groupRepository).resolve(artifactPath);
        ((Maven2LayoutProvider) layoutProvider).deleteFromIndex(repositoryPath);
    }

    @Override
    protected UpdateCallback newInstance()
    {
        return new IndexUpdateCallback();
    }

    class IndexUpdateCallback
            implements UpdateCallback
    {

        @Override
        public void performUpdate(final RepositoryPath parentRepositoryArtifactAbsolutePath)
                throws IOException
        {
            artifactIndexesService.addArtifactToIndex(parentRepositoryArtifactAbsolutePath);
        }
    }

}
