package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.client.MutableRemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.client.RemoteRepositoryRetryArtifactDownloadConfiguration;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressFBWarnings(value = "AJCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
public class RemoteRepositoriesConfiguration
{

    private RemoteRepositoryRetryArtifactDownloadConfiguration remoteRepositoryRetryArtifactDownloadConfiguration;

    private int checkIntervalSeconds;

    private int heartbeatThreadsNumber;

    RemoteRepositoriesConfiguration()
    {

    }

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
