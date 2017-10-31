package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.repository.MavenGroupRepositoryComponent;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.nio.file.Path;
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
    MavenGroupRepositoryComponent mavenGroupRepositoryComponent;

    Repository getRepository(final ArtifactEvent event)
    {
        return configurationManager.getConfiguration().getStorage(event.getStorageId()).getRepository(
                event.getRepositoryId());
    }

    void updateParentGroupRepositoriesMetadata(final ArtifactEvent event,
                                               final Function<Path, Path> artifactBasePathCalculation)
    {
        try
        {
            mavenGroupRepositoryComponent.updateMetadataInRepositoryParents(event.getStorageId(),
                                                                            event.getRepositoryId(),
                                                                            event.getPath(),
                                                                            artifactBasePathCalculation);
        }
        catch (Exception e)
        {
            logger.error("Unable to update parent group repositories metadata of file " + event.getPath() +
                         " of repository " + event.getRepositoryId(), e);
        }
    }

}
