package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator.RepositoryIndexCreatorQualifier;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexDirectoryPathResolver;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexDirectoryPathResolver.RepositoryIndexDirectoryPathResolverQualifier;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;

import javax.inject.Inject;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.index.context.IndexingContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Martin Todorov
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
public class DownloadRemoteMavenIndexCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String REPOSITORY_RELEASES = "drmicj-releases";

    private static final String REPOSITORY_PROXIED_RELEASES = "drmicj-proxied-releases";

    private static final String PROXY_REPOSITORY_URL = "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES + "/";

    private static final String GROUP_ID = "org.carlspring.strongbox.indexes.download";

    private static final String ARTIFACT_ID1 = "strongbox-test-one";

    private static final String ARTIFACT_ID2 = "strongbox-test-two";

    private static final String VERSION = "1.0";

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.HOSTED)
    private RepositoryIndexCreator repositoryIndexCreator;

    @Inject
    @RepositoryIndexDirectoryPathResolverQualifier(IndexTypeEnum.REMOTE)
    private RepositoryIndexDirectoryPathResolver repositoryIndexDirectoryPathResolver;

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testDownloadRemoteIndexAndExecuteSearch(@MavenRepository(repositoryId = REPOSITORY_RELEASES,
                                                                         setup = MavenIndexedRepositorySetup.class)
                                                        Repository repository,
                                                        @Remote(url = PROXY_REPOSITORY_URL)
                                                        @MavenRepository(repositoryId = REPOSITORY_PROXIED_RELEASES,
                                                                         setup = MavenIndexedRepositorySetup.class)
                                                        Repository proxyRepository,
                                                        @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                           id = GROUP_ID + ":" + ARTIFACT_ID1,
                                                                           versions = { VERSION })
                                                        Path artifact1,
                                                        @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                           id = GROUP_ID + ":" + ARTIFACT_ID2,
                                                                           versions = { VERSION })
                                                        Path artifact2)
            throws Exception
    {
        final String storageId = proxyRepository.getStorage().getId();
        final String repositoryId = proxyRepository.getId();

        repositoryIndexCreator.apply(repository);

        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        // Checking if job was executed
        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
            {
                try
                {
                    RepositoryPath remoteIndexDirectory = repositoryIndexDirectoryPathResolver.resolve(proxyRepository);
                    RepositoryPath packedIndexPath = remoteIndexDirectory.resolve(IndexingContext.INDEX_FILE_PREFIX + ".gz");
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
                         DownloadRemoteMavenIndexCronJobTestSubj.class,
                         storageId,
                         repositoryId);

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    public static class DownloadRemoteMavenIndexCronJobTestSubj
            extends DownloadRemoteMavenIndexCronJob
    {

        @Override
        public boolean enabled(CronTaskConfigurationDto configuration,
                               Environment env)
        {
            return true;
        }

    }

}
