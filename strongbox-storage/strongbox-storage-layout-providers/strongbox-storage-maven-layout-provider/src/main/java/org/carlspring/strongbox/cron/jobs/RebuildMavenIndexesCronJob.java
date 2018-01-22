package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.services.ArtifactIndexesService;

import javax.inject.Inject;

/**
 * @author Kate Novik.
 */
public class RebuildMavenIndexesCronJob
        extends OnePerRepositoryJavaCronJob
{

    @Inject
    private ArtifactIndexesService artifactIndexesService;

    @Override
    public void executeTask(CronTaskConfiguration config)
            throws Throwable
    {
        logger.debug("Executing RebuildMavenIndexesCronJob ...");

        String storageId = config.getRequiredProperty("storageId");
        String repositoryId = config.getRequiredProperty("repositoryId");
        String basePath = config.getProperty("basePath");

        artifactIndexesService.rebuildIndex(storageId, repositoryId, basePath);
    }

}
