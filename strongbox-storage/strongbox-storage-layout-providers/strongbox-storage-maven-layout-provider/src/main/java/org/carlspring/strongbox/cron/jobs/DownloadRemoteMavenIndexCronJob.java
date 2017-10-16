package org.carlspring.strongbox.cron.jobs;

import javax.inject.Inject;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kate Novik
 * @author carlspring
 */
public class DownloadRemoteMavenIndexCronJob
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(DownloadRemoteMavenIndexCronJob.class);

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Override
    public void executeTask(CronTaskConfiguration config)
            throws Throwable
    {
        logger.debug("Executing DownloadRemoteIndexCronJob.");

        String storageId = config.getProperty("storageId");
        String repositoryId = config.getProperty("repositoryId");

        Storage storage = configurationManagementService.getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        MavenRepositoryFeatures features = (MavenRepositoryFeatures) layoutProvider.getRepositoryFeatures();
        features.downloadRemoteIndex(storageId, repositoryId);
    }

}
