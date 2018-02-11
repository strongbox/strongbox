package org.carlspring.strongbox.storage.repository.remote.heartbeat.monitor;

/**
 * @author Przemyslaw Fusik
 */
@FunctionalInterface
public interface RemoteRepositoryHeartbeatMonitorStrategy
{

    boolean isAlive(String remoteRepositoryUrl);
}
