package org.carlspring.strongbox.providers.repository.proxy;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class RemoteRepositoryStatusCheckManager
{

    public static final Logger logger = LoggerFactory.getLogger(RemoteRepositoryStatusCheckManager.class);

    @Inject
    private RemoteRepositoryRegistry remoteRepositoryRegistry;

    // TODO: Make this configurable via the strongbox.xml
    private long checkInterval = 60000L;

    private boolean executeChecks = true;


    public RemoteRepositoryStatusCheckManager()
    {
    }

    //@PostConstruct
    public void execute()
    {
        while (executeChecks)
        {
            try
            {
                checkAllRemoteRepositories();

                Thread.sleep(checkInterval);
            }
            catch (InterruptedException e)
            {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void checkAllRemoteRepositories()
    {
        for (RemoteRepositoryStatusInfo info : remoteRepositoryRegistry.getRemoteRepositoryStatusInfos().values())
        {
            if (RemoteRepositoryStatusEnum.UNKNOWN.getStatus().equals(info.getStatus()) ||
                System.currentTimeMillis() >= info.getLastCheckedStatus() + checkInterval)
            {

            }
        }
    }

    public long getCheckInterval()
    {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval)
    {
        this.checkInterval = checkInterval;
    }

}
