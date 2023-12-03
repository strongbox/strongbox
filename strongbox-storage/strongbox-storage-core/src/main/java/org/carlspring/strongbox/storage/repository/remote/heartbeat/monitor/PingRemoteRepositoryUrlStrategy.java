package org.carlspring.strongbox.storage.repository.remote.heartbeat.monitor;

import static org.carlspring.strongbox.utils.Ping.pingHost;

/**
 * @author Przemyslaw Fusik
 */
enum PingRemoteRepositoryUrlStrategy
        implements RemoteRepositoryHeartbeatMonitorStrategy
{

    INSTANCE;

    @Override
    public boolean isAlive(String remoteRepositoryUrl)
    {
        return pingHost(remoteRepositoryUrl, 5000);
    }
}
