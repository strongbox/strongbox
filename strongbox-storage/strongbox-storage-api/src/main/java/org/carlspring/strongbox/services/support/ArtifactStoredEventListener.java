package org.carlspring.strongbox.services.support;

import org.carlspring.strongbox.event.artifact.ArtifactEvent;
import org.carlspring.strongbox.event.artifact.ArtifactEventListener;
import org.carlspring.strongbox.event.artifact.ArtifactEventTypeEnum;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ArtifactStoredEventListener
        implements ArtifactEventListener<RepositoryPath>
{

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Async
    @Override
    public void handle(final ArtifactEvent<RepositoryPath> event)
    {
        if (event.getType() != ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_STORED.getType())
        {
            return;
        }

        final RepositoryPath repositoryPath = event.getPath();
        final Repository repository = repositoryPath.getRepository();
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
    }
}
