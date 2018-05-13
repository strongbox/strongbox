package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.jobs.DownloadRemoteMavenIndexCronJob;
import org.carlspring.strongbox.cron.jobs.MergeMavenGroupRepositoryIndexCronJob;
import org.carlspring.strongbox.cron.jobs.RebuildMavenIndexesCronJob;
import org.carlspring.strongbox.cron.services.CronTaskDataService;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class MavenRepositoryManagementStrategy
        extends AbstractRepositoryManagementStrategy
{

    @Inject
    private CronTaskDataService cronTaskDataService;

    @Inject
    private MavenRepositoryFeatures repositoryFeatures;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Override
    protected void createRepositoryInternal(Storage storage,
                                            Repository repository)
            throws RepositoryManagementStrategyException
    {
        if (!repositoryFeatures.isIndexingEnabled(repository))
        {
            return;
        }

        String storageId = storage.getId();
        String repositoryId = repository.getId();

        if (repository.isHostedRepository())
        {
            createRebuildMavenIndexCronJob(storageId, repositoryId);
        }
        if (repository.isProxyRepository())
        {
            createRemoteIndexDownloaderCronTask(storageId, repositoryId);
        }
        if (repository.isGroupRepository())
        {
            createMergeMavenGroupRepositoryIndexCronJob(storageId, repositoryId);
        }
    }

    private void createRemoteIndexDownloaderCronTask(String storageId,
                                                     String repositoryId)
            throws RepositoryManagementStrategyException
    {
        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setName("Remote index download for " + storageId + ":" + repositoryId);
        configuration.setJobClass(DownloadRemoteMavenIndexCronJob.class.getName());
        configuration.setCronExpression("0 0 0 * * ?"); // Execute once daily at 00:00:00
        configuration.addProperty("storageId", storageId);
        configuration.addProperty("repositoryId", repositoryId);
        configuration.setImmediateExecution(true);

        try
        {
            cronTaskDataService.save(configuration);
        }
        catch (Exception e)
        {
            throw new RepositoryManagementStrategyException(e.getMessage(), e);
        }
    }

    private void createRebuildMavenIndexCronJob(String storageId,
                                                String repositoryId)
            throws RepositoryManagementStrategyException
    {
        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setName("Rebuild Maven Index Cron Job for " + storageId + ":" + repositoryId);
        configuration.setJobClass(RebuildMavenIndexesCronJob.class.getName());
        configuration.setCronExpression("0 0 2 * * ?");
        configuration.addProperty("storageId", storageId);
        configuration.addProperty("repositoryId", repositoryId);
        configuration.setImmediateExecution(true);

        try
        {
            cronTaskDataService.save(configuration);
        }
        catch (Exception e)
        {
            throw new RepositoryManagementStrategyException(e.getMessage(), e);
        }
    }

    private void createMergeMavenGroupRepositoryIndexCronJob(String storageId,
                                                             String repositoryId)
            throws RepositoryManagementStrategyException
    {
        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setName("Merge maven group repository index cron job " + storageId + ":" + repositoryId);
        configuration.setJobClass(MergeMavenGroupRepositoryIndexCronJob.class.getName());
        configuration.setCronExpression("0 0 4 * * ?");
        configuration.addProperty("storageId", storageId);
        configuration.addProperty("repositoryId", repositoryId);
        configuration.setImmediateExecution(false);

        try
        {
            cronTaskDataService.save(configuration);
        }
        catch (Exception e)
        {
            throw new RepositoryManagementStrategyException(e.getMessage(), e);
        }
    }

    @Override
    public void createRepositoryStructure(final Repository repository)
            throws IOException
    {
        super.createRepositoryStructure(repository);

        final RepositoryPath rootRepositoryPath = repositoryPathResolver.resolve(repository);
        final RepositoryPath indexRepositoryPath = rootRepositoryPath.resolve(".index");
        if (!Files.exists(indexRepositoryPath))
        {
            Files.createDirectories(indexRepositoryPath);
        }
    }

}
