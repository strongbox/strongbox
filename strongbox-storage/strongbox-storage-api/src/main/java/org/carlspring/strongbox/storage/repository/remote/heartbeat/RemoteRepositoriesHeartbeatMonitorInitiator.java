package org.carlspring.strongbox.storage.repository.remote.heartbeat;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.log.CronTaskContextFilter;
import org.carlspring.strongbox.log.LoggingUtils;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class RemoteRepositoriesHeartbeatMonitorInitiator
        implements InitializingBean, DisposableBean
{

    private static final Logger logger = LoggerFactory.getLogger(RemoteRepositoriesHeartbeatMonitorInitiator.class);

    private ScheduledExecutorService executor;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private RemoteRepositoryAlivenessCacheManager remoteRepositoryCacheManager;

    @Override
    public void destroy()
            throws Exception
    {
        executor.shutdown();
    }

    @Override
    public void afterPropertiesSet()
            throws Exception
    {
        int heartbeatThreadsNumber = getRemoteRepositoriesHeartbeatThreadsNumber();
        executor = Executors.newScheduledThreadPool(heartbeatThreadsNumber);

        int defaultIntervalSeconds = getDefaultRemoteRepositoriesHeartbeatIntervalSeconds();

        getRemoteRepositories().stream().forEach(rr -> scheduleRemoteRepositoryMonitoring(defaultIntervalSeconds, rr));
    }

    private void scheduleRemoteRepositoryMonitoring(int defaultIntervalSeconds,
                                                    RemoteRepository remoteRepository)
    {
        int intervalSeconds = ObjectUtils.defaultIfNull(remoteRepository.getCheckIntervalSeconds(),
                                                        defaultIntervalSeconds);

        Assert.isTrue(intervalSeconds > 0,
                      "intervalSeconds cannot be negative or zero but was " + intervalSeconds + " for " +
                      remoteRepository.getUrl());

        RemoteRepositoryHeartbeatMonitor remoteRepositoryHeartBeatMonitor = new RemoteRepositoryHeartbeatMonitor(remoteRepositoryCacheManager, remoteRepository);
        executor.scheduleWithFixedDelay(new MdcContextProvider(remoteRepositoryHeartBeatMonitor),
                                        0,
                                        intervalSeconds, TimeUnit.SECONDS);

        logger.info(
                "Remote repository " + remoteRepository.getUrl() + " scheduled for monitoring with interval seconds " +
                intervalSeconds);
    }


    private List<RemoteRepository> getRemoteRepositories()
    {
        return configurationManager.getConfiguration()
                                   .getStorages()
                                   .values()
                                   .stream()
                                   .flatMap(s -> s.getRepositories().values().stream())
                                   .filter(Repository::isProxyRepository)
                                   .map(r -> r.getRemoteRepository())
                                   .collect(Collectors.toList());
    }

    private int getDefaultRemoteRepositoriesHeartbeatIntervalSeconds()
    {
        return configurationManager.getConfiguration().getRemoteRepositoriesConfiguration().getCheckIntervalSeconds();
    }

    private int getRemoteRepositoriesHeartbeatThreadsNumber()
    {
        return configurationManager.getConfiguration().getRemoteRepositoriesConfiguration().getHeartbeatThreadsNumber();
    }
    
    public static class MdcContextProvider implements Runnable
    {

        private Runnable target;

        public MdcContextProvider(Runnable target)
        {
            super();
            this.target = target;
        }

        @Override
        public void run()
        {
            MDC.put(CronTaskContextFilter.STRONGBOX_CRON_CONTEXT_NAME, LoggingUtils.caclucateCronContextName(target.getClass()));
            try
            {
                target.run();
            } 
            finally
            {
                MDC.remove(CronTaskContextFilter.STRONGBOX_CRON_CONTEXT_NAME);
            }
        }

    }
}
