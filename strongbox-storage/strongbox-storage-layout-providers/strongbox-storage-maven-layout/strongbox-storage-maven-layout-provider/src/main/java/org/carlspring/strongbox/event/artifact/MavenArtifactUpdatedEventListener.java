package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.event.AsyncEventListener;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenArtifactUpdatedEventListener
        extends BaseMavenArtifactEventListener
{

    @AsyncEventListener
    public void handle(final ArtifactEvent<RepositoryPath> event)
    {
        final Repository repository = getRepository(event);

        if (!Maven2LayoutProvider.ALIAS.equals(repository.getLayout()))
        {
            return;
        }

        if (event.getType() != ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_UPDATED.getType())
        {
            return;
        }

        updateMetadataInGroupsContainingRepository(event, RepositoryPath::getParent);
    }

}
