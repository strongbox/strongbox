package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.locator.handlers.GenerateMavenMetadataOperation;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Pablo Tirado
 */
public class MavenMetadataServiceHelper
{

    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    @Inject
    protected MavenMetadataManager mavenMetadataManager;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;

    public void generateMavenMetadata(Repository repository)
            throws IOException
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setBasedir(repositoryPath);
        locator.setOperation(new GenerateMavenMetadataOperation(mavenMetadataManager, artifactEventListenerRegistry));
        locator.locateArtifactDirectories();
    }
}
