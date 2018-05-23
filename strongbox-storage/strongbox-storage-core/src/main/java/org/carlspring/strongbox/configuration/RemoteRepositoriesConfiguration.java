package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.client.RemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.client.MutableRemoteRepositoryRetryArtifactDownloadConfiguration;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class RemoteRepositoriesConfiguration
{

    private final RemoteRepositoryRetryArtifactDownloadConfiguration remoteRepositoryRetryArtifactDownloadConfiguration;

    private final int checkIntervalSeconds;

    private final int heartbeatThreadsNumber;

    public RemoteRepositoriesConfiguration(final MutableRemoteRepositoriesConfiguration delegate)
    {
        this.remoteRepositoryRetryArtifactDownloadConfiguration = immuteRemoteRepositoryRetryArtifactDownloadConfiguration(
                delegate.getRemoteRepositoryRetryArtifactDownloadConfiguration());
        this.checkIntervalSeconds = delegate.getCheckIntervalSeconds();
        this.heartbeatThreadsNumber = delegate.getHeartbeatThreadsNumber();
    }

    public RemoteRepositoryRetryArtifactDownloadConfiguration getRemoteRepositoryRetryArtifactDownloadConfiguration()
    {
        return remoteRepositoryRetryArtifactDownloadConfiguration;
    }

    private RemoteRepositoryRetryArtifactDownloadConfiguration immuteRemoteRepositoryRetryArtifactDownloadConfiguration(final MutableRemoteRepositoryRetryArtifactDownloadConfiguration source)
    {
        return source != null ? new RemoteRepositoryRetryArtifactDownloadConfiguration(source) : null;
    }

    public int getCheckIntervalSeconds()
    {
        return checkIntervalSeconds;
    }

    public int getHeartbeatThreadsNumber()
    {
        return heartbeatThreadsNumber;
    }
}
