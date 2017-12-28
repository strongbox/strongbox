package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.repository.group.metadata.MavenMetadataGroupRepositoryComponent;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Przemyslaw Fusik
 */
abstract class BaseMavenArtifactEventListener
        implements ArtifactEventListener
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    ConfigurationManager configurationManager;

    @Inject
    ArtifactMetadataService artifactMetadataService;

    @Inject
    LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    MavenMetadataManager mavenMetadataManager;

    @Inject
    MavenMetadataGroupRepositoryComponent mavenMetadataGroupRepositoryComponent;

    Repository getRepository(final ArtifactEvent event)
    {
        return configurationManager.getConfiguration().getStorage(event.getStorageId()).getRepository(
                event.getRepositoryId());
    }

    void updateMetadataInGroupsContainingRepository(final ArtifactEvent event,
                                                    final Function<RepositoryPath, RepositoryPath> artifactBasePathCalculation)
    {

        final Repository repository = getRepository(event);
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final RepositoryPath repositoryAbsolutePath = layoutProvider.resolve(repository);
        final RepositoryPath artifactAbsolutePath = repositoryAbsolutePath.resolve(event.getPath());
        final RepositoryPath artifactBasePath = artifactBasePathCalculation.apply(artifactAbsolutePath);

        try
        {
            mavenMetadataGroupRepositoryComponent.updateGroupsContaining(event.getStorageId(),
                                                                         event.getRepositoryId(),
                                                                         artifactBasePath.relativize().toString());
        }
        catch (Exception e)
        {
            logger.error("Unable to update parent group repositories metadata of file " + event.getPath() +
                         " of repository " + event.getRepositoryId(), e);
        }
    }

}
