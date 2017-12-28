package org.carlspring.strongbox.event.artifact;


import org.carlspring.strongbox.repository.group.index.MavenIndexGroupRepositoryComponent;
import org.carlspring.strongbox.repository.group.metadata.MavenMetadataGroupRepositoryComponent;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;

import javax.inject.Inject;
import java.io.IOException;

import com.google.common.base.Throwables;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenArtifactDeletedEventListener
        extends BaseMavenArtifactEventListener
{


    @Inject
    private MavenIndexGroupRepositoryComponent mavenIndexGroupRepositoryComponent;

    @Override
    public void handle(final ArtifactEvent event)
    {
        final Repository repository = getRepository(event);

        if (!RepositoryLayoutEnum.MAVEN_2.getLayout().equals(repository.getLayout()))
        {
            return;
        }

        if (event.getType() != ArtifactEventTypeEnum.EVENT_ARTIFACT_PATH_DELETED.getType())
        {
            return;
        }

        try
        {
            mavenMetadataGroupRepositoryComponent.cleanupGroupsContaining(event.getStorageId(),
                                                                          event.getRepositoryId(),
                                                                          event.getPath());


            mavenIndexGroupRepositoryComponent.cleanupGroupsContaining(event.getStorageId(),
                                                                       event.getRepositoryId(),
                                                                       event.getPath());

        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
    }
}
