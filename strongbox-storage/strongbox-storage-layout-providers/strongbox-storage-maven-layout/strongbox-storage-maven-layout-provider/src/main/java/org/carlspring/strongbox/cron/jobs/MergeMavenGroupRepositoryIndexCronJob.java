package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.jobs.fields.*;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator.RepositoryIndexCreatorQualifier;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import javax.inject.Inject;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
public class MergeMavenGroupRepositoryIndexCronJob
        extends JavaCronJob
{

    private static final String PROPERTY_STORAGE_ID = "storageId";

    private static final String PROPERTY_REPOSITORY_ID = "repositoryId";

    private static final Set<CronJobField> FIELDS = ImmutableSet.of(
            new CronJobStorageIdAutocompleteField(new CronJobStringTypeField(
                    new CronJobRequiredField(new CronJobNamedField(PROPERTY_STORAGE_ID)))),
            new CronJobRepositoryIdAutocompleteField(new CronJobStringTypeField(
                    new CronJobRequiredField(new CronJobNamedField(PROPERTY_REPOSITORY_ID)))));

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.GROUP)
    private RepositoryIndexCreator repositoryIndexCreator;

    @Inject
    private ConfigurationManager configurationManager;

    @Override
    public void executeTask(CronTaskConfigurationDto config)
            throws Throwable
    {
        String storageId = config.getProperty(PROPERTY_STORAGE_ID);
        String repositoryId = config.getProperty(PROPERTY_REPOSITORY_ID);

        logger.debug("Executing MergeMavenGroupRepositoryIndexCronJob for storageId = [{}], repositoryId = [{}]",
                     storageId, repositoryId);

        Repository repository = configurationManager.getRepository(storageId, repositoryId);

        if (!repository.isGroupRepository())
        {
            logger.warn("Repository identified by storageId = [{}], repositoryId = [{}] is not a group repository. Exiting ...",
                        storageId, repositoryId);
            return;
        }

        repositoryIndexCreator.apply(repository);
    }

    @Override
    public CronJobDefinition getCronJobDefinition()
    {
        return CronJobDefinition.newBuilder()
                                .jobClass(MergeMavenGroupRepositoryIndexCronJob.class.getName())
                                .name("Merge Maven Group Repository Index Cron Job")
                                .description("Merges all indexes belonging to the repositories of the group repository")
                                .fields(FIELDS)
                                .build();
    }
}
