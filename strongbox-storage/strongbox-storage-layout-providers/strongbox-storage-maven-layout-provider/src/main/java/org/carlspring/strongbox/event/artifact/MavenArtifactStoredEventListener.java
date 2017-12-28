package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.group.index.MavenIndexGroupRepositoryComponent;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenArtifactStoredEventListener
        extends BaseMavenArtifactEventListener
{

    @Inject
    private MavenIndexGroupRepositoryComponent mavenIndexGroupRepositoryComponent;

    @Override
    public void handle(final ArtifactEvent event)
    {
        final Repository repository = getRepository(event);

        if (!Maven2LayoutProvider.ALIAS.equals(repository.getLayout()))
        {
            return;
        }

        if (event.getType() != ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_STORED.getType())
        {
            return;
        }

        try
        {
            mavenIndexGroupRepositoryComponent.updateGroupsContaining(event.getStorageId(),
                                                                      event.getRepositoryId(),
                                                                      event.getPath());
        }
        catch (final IOException e)
        {
            logger.error("Unable to update parent group repositories indexes of file " + event.getPath() +
                         " of repository " + event.getRepositoryId(), e);
        }
    }
}
