package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;

import java.nio.file.Path;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenArtifactUploadedEventListener
        extends BaseMavenArtifactEventListener
{

    @Override
    public void handle(final ArtifactEvent event)
    {
        final Repository repository = getRepository(event);

        if (!RepositoryLayoutEnum.MAVEN_2.getLayout().equals(repository.getLayout()))
        {
            return;
        }

        if (event.getType() != ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_UPLOADED.getType())
        {
            return;
        }

        updateMetadataInGroupsContainingRepository(event, Path::getParent);
    }
}
