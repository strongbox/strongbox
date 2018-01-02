package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.services.ArtifactIndexesService;

import javax.inject.Inject;

/**
 * @author Kate Novik.
 */
public class RebuildMavenIndexesCronJob
        extends JavaCronJob
{

    @Inject
    private ArtifactIndexesService artifactIndexesService;

    @Override
    public void executeTask(CronTaskConfiguration config)
            throws Throwable
    {
        logger.debug("Executed RebuildMavenIndexesCronJob.");

        String storageId = config.getProperty("storageId");
        String repositoryId = config.getProperty("repositoryId");
        String basePath = config.getProperty("basePath");

        if (storageId == null)
        {
            artifactIndexesService.rebuildIndexes();
        }
        else if (repositoryId == null)
        {
            artifactIndexesService.rebuildIndexes(storageId);
        }
        else
        {
            artifactIndexesService.rebuildIndex(storageId, repositoryId, basePath);
        }
    }

}
