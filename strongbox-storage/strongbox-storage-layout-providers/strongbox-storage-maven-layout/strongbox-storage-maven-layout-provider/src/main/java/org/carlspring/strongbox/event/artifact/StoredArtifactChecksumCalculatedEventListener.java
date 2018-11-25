package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.event.AsyncEventListener;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.repository.event.StoredArtifactChecksumCalculatedEvent;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class StoredArtifactChecksumCalculatedEventListener
        extends BaseMavenArtifactEventListener
{

    @Inject
    private ArtifactManagementService artifactManagementService;

    @AsyncEventListener
    public void handle(final StoredArtifactChecksumCalculatedEvent event)
    {
        final Repository repository = getRepository(event);

        if (!Maven2LayoutProvider.ALIAS.equals(repository.getLayout()))
        {
            return;
        }

        RepositoryPath repositoryPath = (RepositoryPath) event.getPath();

        event.getDigestMap()
             .entrySet()
             .stream()
             .forEach(entry -> {
                 try (InputStream is = new ByteArrayInputStream(entry.getValue().getBytes(StandardCharsets.UTF_8)))
                 {
                     // TODO
                     RepositoryPath checksumPath = repositoryPath.resolveSibling(repositoryPath.getFileName() + "." + entry.getKey());
                     artifactManagementService.store(checksumPath, is);
                 }
                 catch (IOException e)
                 {
                     logger.error(e.getMessage(), e);
                 }
             });

    }
}
