package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.MavenIndexerEnabledCondition;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;

import org.springframework.core.env.Environment;

/**
 * @author Kate Novik.
 */
public class RebuildMavenIndexesCronJob
        extends JavaCronJob
{

    @Inject
    private ArtifactIndexesService artifactIndexesService;
    
    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;
    
    @Override
    public void executeTask(CronTaskConfigurationDto config)
            throws Throwable
    {
        logger.debug("Executing RebuildMavenIndexesCronJob ...");

        String storageId = config.getRequiredProperty("storageId");
        String repositoryId = config.getRequiredProperty("repositoryId");
        String basePath = config.getProperty("basePath");

        Storage storage = layoutProviderRegistry.getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, basePath);
        
        artifactIndexesService.rebuildIndex(repositoryPath);
    }

    @Override
    public boolean enabled(CronTaskConfigurationDto configuration, Environment env)
    {
        if (!super.enabled(configuration, env))
        {
            return false;
        }
        
        return Boolean.parseBoolean(env.getProperty(MavenIndexerEnabledCondition.MAVEN_INDEXER_ENABLED));
    }
    
}
