package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexDirectoryPathResolver;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexDirectoryPathResolver.RepositoryIndexDirectoryPathResolverQualifier;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.index.context.IndexingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Kate Novik.
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
public class RebuildMavenIndexesCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String REPOSITORY_RELEASES_1 = "rmicj-releases";

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_INDEXES = "org/carlspring/strongbox/indexes/strongbox-test-one";

    private static final String GROUP_ID = "org.carlspring.strongbox.indexes.download";

    private static final String ARTIFACT_ID1 = "strongbox-test-one";

    private static final String ARTIFACT_ID2 = "strongbox-test-two";

    private static final String VERSION = "1.0";

    @Inject
    @RepositoryIndexDirectoryPathResolverQualifier(IndexTypeEnum.LOCAL)
    private RepositoryIndexDirectoryPathResolver repositoryIndexDirectoryPathResolver;

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testRebuildArtifactsIndexes(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                             setup = MavenIndexedRepositorySetup.class)
                                            Repository repository,
                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                               id = GROUP_ID + ":" + ARTIFACT_ID1,
                                                               versions = { VERSION })
                                            Path artifact1,
                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                               id = GROUP_ID + ":" + ARTIFACT_ID2,
                                                               versions = { VERSION })
                                            Path artifact2)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        // Checking if job was executed
        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
            {

                RepositoryPath indexPath = repositoryIndexDirectoryPathResolver.resolve(repository);
                RepositoryPath packedIndexPath = indexPath.resolve(IndexingContext.INDEX_FILE_PREFIX + ".gz");
                assertThat(packedIndexPath).matches(Files::exists);
            }
        });

        addCronJobConfig(jobKey,
                         jobName,
                         RebuildMavenIndexesCronJob.class,
                         storageId,
                         repositoryId,
                         properties -> properties.put("basePath", ARTIFACT_BASE_PATH_STRONGBOX_INDEXES));

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testRebuildIndexesInRepository(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                                setup = MavenIndexedRepositorySetup.class)
                                               Repository repository,
                                               @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                                  id = GROUP_ID + ":" + ARTIFACT_ID1,
                                                                  versions = { VERSION })
                                               Path artifact1,
                                               @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                                  id = GROUP_ID + ":" + ARTIFACT_ID2,
                                                                  versions = { VERSION })
                                               Path artifact2)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
            {
                try
                {
                    RepositoryPath indexPath = repositoryIndexDirectoryPathResolver.resolve(repository);
                    RepositoryPath packedIndexPath = indexPath.resolve(IndexingContext.INDEX_FILE_PREFIX + ".gz");
                    assertThat(packedIndexPath).matches(Files::exists);
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobKey,
                         jobName,
                         RebuildMavenIndexesCronJob.class,
                         storageId,
                         repositoryId);

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

}
