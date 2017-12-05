package org.carlspring.strongbox.services.support;

import org.carlspring.strongbox.services.ArtifactByteStreamsCopyStrategy;
import org.carlspring.strongbox.services.impl.SimpleArtifactByteStreamsCopy;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ArtifactByteStreamsCopyStrategyDeterminator
{

    @Inject
    private ArtifactByteStreamsCopyStrategy proxyRepositoryArtifactByteStreamsCopy;

    private ArtifactByteStreamsCopyStrategy simpleArtifactByteStreams = SimpleArtifactByteStreamsCopy.INSTANCE;

    public ArtifactByteStreamsCopyStrategy determine(final Repository repository)
    {
        return repository.isProxyRepository() ? proxyRepositoryArtifactByteStreamsCopy : simpleArtifactByteStreams;

    }

}
