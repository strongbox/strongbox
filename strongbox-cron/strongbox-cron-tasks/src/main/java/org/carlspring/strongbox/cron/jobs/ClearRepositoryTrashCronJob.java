package org.carlspring.strongbox.cron.jobs;

import javax.inject.Inject;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kate Novik.
 */
public class ClearRepositoryTrashCronJob
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(ClearRepositoryTrashCronJob.class);

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Override
    public void executeTask(CronTaskConfiguration config)
            throws Throwable
    {
        String storageId = config.getProperty("storageId");
        String repositoryId = config.getProperty("repositoryId");

        if (storageId == null && repositoryId == null)
        {
            repositoryManagementService.deleteTrash();
        }
        else
        {
            repositoryManagementService.deleteTrash(storageId, repositoryId);
        }
    }

}
