package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;

/**
 * @author Przemyslaw Fusik
 */
abstract class BaseMavenArtifactEventListener
        implements ArtifactEventListener
{

    @Inject
    ConfigurationManager configurationManager;

    @Inject
    ArtifactMetadataService artifactMetadataService;

    @Inject
    LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    MavenMetadataManager mavenMetadataManager;

    Repository getRepository(final ArtifactEvent event)
    {
        return configurationManager.getConfiguration().getStorage(event.getStorageId()).getRepository(
                event.getRepositoryId());
    }

}
