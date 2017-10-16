package org.carlspring.strongbox.cron.jobs;

import javax.inject.Inject;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kate Novik.
 */
public class RebuildMavenIndexesCronJob
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(RebuildMavenIndexesCronJob.class);

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
