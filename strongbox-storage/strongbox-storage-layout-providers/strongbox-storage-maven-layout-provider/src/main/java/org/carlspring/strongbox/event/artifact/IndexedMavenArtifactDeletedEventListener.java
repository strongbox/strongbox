package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.config.MavenIndexerEnabledCondition;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.group.index.MavenIndexGroupRepositoryComponent;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;

import com.google.common.base.Throwables;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
@Conditional(MavenIndexerEnabledCondition.class)
public class IndexedMavenArtifactDeletedEventListener
        extends BaseMavenArtifactEventListener
{

    @Inject
    private MavenIndexGroupRepositoryComponent mavenIndexGroupRepositoryComponent;

    @Override
    public void handle(final ArtifactEvent<RepositoryPath> event)
    {
        final Repository repository = getRepository(event);

        if (!Maven2LayoutProvider.ALIAS.equals(repository.getLayout()))
        {
            return;
        }

        if (event.getType() != ArtifactEventTypeEnum.EVENT_ARTIFACT_PATH_DELETED.getType())
        {
            return;
        }

        try
        {
            mavenIndexGroupRepositoryComponent.cleanupGroupsContaining(event.getPath());
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
    }
}
