package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.repository.MavenGroupRepositoryComponent;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;

import javax.inject.Inject;
import java.io.IOException;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenArtifactDeletedEventListener
        extends BaseMavenArtifactEventListener
{

    private static final Logger logger = LoggerFactory.getLogger(MavenArtifactDeletedEventListener.class);

    @Inject
    private MavenGroupRepositoryComponent mavenGroupRepositoryComponent;

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
            mavenGroupRepositoryComponent.deleteMetadataInRepositoryParents(event.getStorageId(),
                                                                            event.getRepositoryId(),
                                                                            event.getPath());
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
    }
}
