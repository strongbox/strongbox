package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;

import javax.inject.Inject;

/**
 * @author Pablo Tirado
 */
abstract class BaseMavenRepositoryProviderTest
{
    @Inject
    protected MavenMetadataManager mavenMetadataManager;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;

    @Inject
    protected ConfigurationManagementService configurationManagementService;

    @Inject
    protected RepositoryPathResolver repositoryPathResolver;
}
