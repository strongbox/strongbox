package org.carlspring.strongbox.storage.repository.remote.heartbeat.monitor;

/**
 * @author Przemyslaw Fusik
 */
@FunctionalInterface
public interface RemoteRepositoryHeartbeatMonitorStrategy
{

    static RemoteRepositoryHeartbeatMonitorStrategy of(boolean allowsDirectoryBrowsing)
    {
        return allowsDirectoryBrowsing ? HttpGetRemoteRepositoryCheckStrategy.INSTANCE :
               PingRemoteRepositoryUrlStrategy.INSTANCE;
    }

    boolean isAlive(String remoteRepositoryUrl);
}
