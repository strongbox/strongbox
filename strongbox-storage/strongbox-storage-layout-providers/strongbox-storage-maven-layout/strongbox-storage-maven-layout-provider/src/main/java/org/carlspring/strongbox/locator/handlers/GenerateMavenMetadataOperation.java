package org.carlspring.strongbox.locator.handlers;

import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.metadata.VersionCollectionRequest;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class GenerateMavenMetadataOperation
        extends AbstractMavenArtifactLocatorOperation
{

    private static final Logger logger = LoggerFactory.getLogger(GenerateMavenMetadataOperation.class);

    private final MavenMetadataManager mavenMetadataManager;

    private final ArtifactEventListenerRegistry artifactEventListenerRegistry;


    public GenerateMavenMetadataOperation(@Nonnull final MavenMetadataManager mavenMetadataManager,
                                          @Nonnull final ArtifactEventListenerRegistry artifactEventListenerRegistry)
    {
        Objects.requireNonNull(mavenMetadataManager);
        Objects.requireNonNull(artifactEventListenerRegistry);
        this.mavenMetadataManager = mavenMetadataManager;
        this.artifactEventListenerRegistry = artifactEventListenerRegistry;
    }

    @Override
    public void executeOperation(VersionCollectionRequest request,
                                 RepositoryPath artifactGroupDirectoryPath,
                                 List<RepositoryPath> versionDirectories)
    {
        try
        {
            mavenMetadataManager.generateMetadata(artifactGroupDirectoryPath, request);
            artifactEventListenerRegistry.dispatchArtifactMetadataStoredEvent(artifactGroupDirectoryPath.resolve("maven-metadata.xml"));
        }
        catch (Exception e)
        {
            logger.error("Failed to generate metadata for {}", artifactGroupDirectoryPath, e);
        }
    }

}
