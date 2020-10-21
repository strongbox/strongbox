package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.io.RepositoryPath;
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
public abstract class BaseMavenArtifactEventListener
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

    Repository getRepository(final ArtifactEvent<RepositoryPath> event)
    {
        return event.getPath().getFileSystem().getRepository();
    }

    void updateMetadataInGroupsContainingRepository(final ArtifactEvent<RepositoryPath> event,
                                                    final Function<RepositoryPath, RepositoryPath> artifactBasePathCalculation)
    {

        RepositoryPath artifactBasePath = artifactBasePathCalculation.apply(event.getPath());
        try
        {
            mavenMetadataGroupRepositoryComponent.updateGroupsContaining(artifactBasePath);
        }
        catch (Exception e)
        {
            logger.error("Unable to update parent group repositories metadata of file {}", event.getPath(), e);
        }
    }

}
