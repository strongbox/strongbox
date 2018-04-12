package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;

/**
 * @author Kate Novik.
 */
public class RebuildMavenIndexesCronJob
        extends OnePerRepositoryJavaCronJob
{

    @Inject
    private ArtifactIndexesService artifactIndexesService;
    
    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Override
    public void executeTask(CronTaskConfiguration config)
            throws Throwable
    {
        logger.debug("Executing RebuildMavenIndexesCronJob ...");

        String storageId = config.getRequiredProperty("storageId");
        String repositoryId = config.getRequiredProperty("repositoryId");
        String basePath = config.getProperty("basePath");

        Storage storage = layoutProviderRegistry.getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        LayoutProvider provider = layoutProviderRegistry.getProvider(repository.getLayout());
        RepositoryPath repositoryPath = provider.resolve(repository).resolve(basePath);
        
        artifactIndexesService.rebuildIndex(repositoryPath);
    }

}
