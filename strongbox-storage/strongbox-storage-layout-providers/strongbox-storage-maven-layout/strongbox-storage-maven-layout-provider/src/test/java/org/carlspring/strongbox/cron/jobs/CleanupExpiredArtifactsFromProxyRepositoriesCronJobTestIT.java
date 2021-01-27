package org.carlspring.strongbox.cron.jobs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.lang.time.DateUtils;
import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.repository.ProxyRepositoryProvider;
import org.carlspring.strongbox.repositories.ArtifactRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;
import org.codehaus.plexus.util.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class CleanupExpiredArtifactsFromProxyRepositoriesCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String STORAGE_ID = "storage-common-proxies";

    private static final String REPOSITORY_ID = "maven-central";

    private static final String CENTRAL_URL = "https://repo1.maven.org/maven2/";

    @Inject
    private ProxyRepositoryProvider proxyRepositoryProvider;

    @Inject
    private ArtifactRepository artifactEntityRepository;

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    void expiredArtifactsCleanupCronJobShouldCleanupDatabaseAndStorage(
            @Remote(url = CENTRAL_URL)
            @MavenRepository(storageId = STORAGE_ID,
                             repositoryId = REPOSITORY_ID + "-expiredArtifactsCleanupCronJobShouldCleanupDatabaseAndStorage")
            Repository repository)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        final String artifactPathStr = "org/carlspring/properties-injector/1.5/properties-injector-1.5.jar";

        Optional<Artifact> artifactEntryOptional = Optional.ofNullable(artifactEntityRepository.findOneArtifact(storageId,
                                                                                                                repositoryId,
                                                                                                                artifactPathStr));
        assertThat(artifactEntryOptional).isEqualTo(Optional.empty());

        Path repositoryPath = proxyRepositoryProvider.fetchPath(repositoryPathResolver.resolve(storageId,
                                                                                               repositoryId,
                                                                                               artifactPathStr));
        try (final InputStream ignored = proxyRepositoryProvider.getInputStream(repositoryPath))
        {
        }

        artifactEntryOptional = Optional.ofNullable(artifactEntityRepository.findOneArtifact(storageId,
                                                                                             repositoryId,
                                                                                             artifactPathStr));
        Artifact artifactEntry = artifactEntryOptional.orElse(null);
        assertThat(artifactEntry).isNotNull();
        assertThat(artifactEntry.getLastUpdated()).isNotNull();
        assertThat(artifactEntry.getLastUsed()).isNotNull();
        assertThat(artifactEntry.getSizeInBytes()).isNotNull();
        assertThat(artifactEntry.getSizeInBytes()).isGreaterThan(0L);

        artifactEntry.setLastUsed(artifactEntry.getLastUsed().minusDays(10));
        final Long sizeInBytes = artifactEntry.getSizeInBytes();

        artifactEntityRepository.save(artifactEntry);

        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
            {
                Artifact artifact = artifactEntityRepository.findOneArtifact(storageId,
                                                                             repositoryId,
                                                                             artifactPathStr);
                Optional<Artifact> optionalArtifactEntryFromDb = Optional.ofNullable(artifact);
                assertThat(optionalArtifactEntryFromDb).isEqualTo(Optional.empty());

                try
                {
                    RepositoryPath artifactRepositoryPath = repositoryPathResolver.resolve(repository, artifactPathStr);
                    assertThat(RepositoryFiles.artifactExists(artifactRepositoryPath)).isFalse();

                    RepositoryPath metadataRepositoryPath = artifactRepositoryPath.getParent().resolveSibling(
                            "maven-metadata.xml");
                    assertThat(RepositoryFiles.artifactExists(metadataRepositoryPath)).isTrue();
                }
                catch (IOException e)
                {
                    throw new UndeclaredThrowableException(e);
                }

            }
        });

        addCronJobConfig(jobKey,
                         jobName,
                         CleanupExpiredArtifactsFromProxyRepositoriesCronJob.class,
                         storageId,
                         repositoryId,
                         properties ->
                         {
                             properties.put("lastAccessedTimeInDays", "5");
                             properties.put("minSizeInBytes", Long.valueOf(sizeInBytes - 1).toString());
                         });

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

}
