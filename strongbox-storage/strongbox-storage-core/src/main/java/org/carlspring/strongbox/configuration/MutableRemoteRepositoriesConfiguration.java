package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.client.MutableRemoteRepositoryRetryArtifactDownloadConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "remote-repositories-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class MutableRemoteRepositoriesConfiguration
        implements Serializable
{

    public static final int DEFAULT_HEARTBEAT_INTERVAL_SECONDS = 60;

    public static final MutableRemoteRepositoriesConfiguration DEFAULT = new MutableRemoteRepositoriesConfiguration()
    {

        @Override
        public void setCheckIntervalSeconds(int checkIntervalSeconds)
        {
            throw new UnsupportedOperationException("DEFAULT RemoteRepositoriesConfiguration is immutable");
        }

        @Override
        public void setHeartbeatThreadsNumber(int heartbeatThreadsNumber)
        {
            throw new UnsupportedOperationException("DEFAULT RemoteRepositoriesConfiguration is immutable");
        }
    };

    @XmlElement(name = "retry-artifact-download-configuration")
    private MutableRemoteRepositoryRetryArtifactDownloadConfiguration remoteRepositoryRetryArtifactDownloadConfiguration = MutableRemoteRepositoryRetryArtifactDownloadConfiguration.DEFAULT;

    @XmlAttribute(name = "check-interval-seconds")
    private int checkIntervalSeconds = DEFAULT_HEARTBEAT_INTERVAL_SECONDS;

    @XmlAttribute(name = "heartbeat-threads-number")
    private int heartbeatThreadsNumber = 5;

    public MutableRemoteRepositoryRetryArtifactDownloadConfiguration getRemoteRepositoryRetryArtifactDownloadConfiguration()
    {
        return remoteRepositoryRetryArtifactDownloadConfiguration;
    }

    public void setRemoteRepositoryRetryArtifactDownloadConfiguration(final MutableRemoteRepositoryRetryArtifactDownloadConfiguration remoteRepositoryRetryArtifactDownloadConfiguration)
    {
        this.remoteRepositoryRetryArtifactDownloadConfiguration = remoteRepositoryRetryArtifactDownloadConfiguration;
    }

    public int getCheckIntervalSeconds()
    {
        return checkIntervalSeconds;
    }

    public void setCheckIntervalSeconds(int checkIntervalSeconds)
    {
        this.checkIntervalSeconds = checkIntervalSeconds;
    }

    public int getHeartbeatThreadsNumber()
    {
        return heartbeatThreadsNumber;
    }

    public void setHeartbeatThreadsNumber(int heartbeatThreadsNumber)
    {
        this.heartbeatThreadsNumber = heartbeatThreadsNumber;
    }
}
