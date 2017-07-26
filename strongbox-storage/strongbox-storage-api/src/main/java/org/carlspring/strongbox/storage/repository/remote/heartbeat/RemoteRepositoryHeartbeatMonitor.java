package org.carlspring.strongbox.storage.repository.remote.heartbeat;

import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.monitor.RemoteRepositoryHeartbeatMonitorStrategy;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Przemyslaw Fusik
 */
class RemoteRepositoryHeartbeatMonitor
        implements Runnable
{

    private static final Logger logger = LoggerFactory.getLogger(RemoteRepositoryHeartbeatMonitor.class);

    private final RemoteRepository remoteRepository;

    private final RemoteRepositoryAlivenessCacheManager remoteRepositoryCacheManager;

    RemoteRepositoryHeartbeatMonitor(@Nonnull RemoteRepositoryAlivenessCacheManager remoteRepositoryCacheManager,
                                     @Nonnull RemoteRepository remoteRepository)
    {
        this.remoteRepositoryCacheManager = remoteRepositoryCacheManager;
        this.remoteRepository = remoteRepository;
    }

    @Override
    public void run()
    {
        boolean isAlive = false;
        try
        {
            RemoteRepositoryHeartbeatMonitorStrategy monitorStrategy = RemoteRepositoryHeartbeatMonitorStrategy.of(
                    remoteRepository.isAllowsDirectoryBrowsing());

            isAlive = monitorStrategy.isAlive(remoteRepository.getUrl());
        }
        catch (Exception ex)
        {
            logger.error("Problem determining remote repository [" + remoteRepository.getUrl() + "] aliveness", ex);
        }

        logger.debug("Thread name is [{}]. Remote repository [{}] is alive ? [{}]", Thread.currentThread().getName(),
                     remoteRepository.getUrl(),
                     isAlive);
        remoteRepositoryCacheManager.put(remoteRepository, isAlive);
    }
}
