package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.services.RepositoryManagementService;

import javax.inject.Inject;

/**
 * @author Kate Novik.
 */
public class ClearRepositoryTrashCronJob
        extends JavaCronJob
{

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Override
    public void executeTask(CronTaskConfigurationDto config)
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
