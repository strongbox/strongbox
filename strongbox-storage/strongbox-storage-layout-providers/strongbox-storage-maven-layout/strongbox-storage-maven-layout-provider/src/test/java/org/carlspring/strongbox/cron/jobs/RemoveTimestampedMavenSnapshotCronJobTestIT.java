package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
public class RemoveTimestampedMavenSnapshotCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String REPOSITORY_SNAPSHOTS = "rtmscj-snapshots";

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_TIMESTAMPED = "org/carlspring/strongbox/strongbox-timestamped-first";

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    private Set<RepositoryDto> getRepositoriesToClean(TestInfo testInfo)
    {
        Set<RepositoryDto> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(REPOSITORY_SNAPSHOTS, testInfo),
                                              Maven2LayoutProvider.ALIAS));
        return repositories;
    }

    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);

        //Create repository rtmscj-snapshots in storage0
        createRepository(STORAGE0,
                         getRepositoryName(REPOSITORY_SNAPSHOTS, testInfo),
                         RepositoryPolicyEnum.SNAPSHOT.getPolicy(),
                         false);

        File repositoryBasedir = getRepositoryBasedir(STORAGE0, getRepositoryName(REPOSITORY_SNAPSHOTS, testInfo));

        createTimestampedSnapshotArtifact(repositoryBasedir.getAbsolutePath(),
                                          "org.carlspring.strongbox",
                                          "strongbox-timestamped-first",
                                          "2.0",
                                          "jar",
                                          null,
                                          3);

        createTimestampedSnapshotArtifact(repositoryBasedir.getAbsolutePath(),
                                          "org.carlspring.strongbox",
                                          "strongbox-timestamped-second",
                                          "2.0",
                                          "jar",
                                          null,
                                          2);

        rebuildArtifactsMetadata(testInfo);
    }

    @AfterEach
    public void removeRepositories(TestInfo testInfo)
            throws Exception
    {
        removeRepositories(getRepositoriesToClean(testInfo));
    }

    private void rebuildArtifactsMetadata(TestInfo testInfo)
            throws Exception
    {
        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                getRepositoryName(REPOSITORY_SNAPSHOTS, testInfo),
                                                "org/carlspring/strongbox/strongbox-timestamped-first");

        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                getRepositoryName(REPOSITORY_SNAPSHOTS, testInfo),
                                                "org/carlspring/strongbox/strongbox-timestamped-second");
    }

    @Test
    public void testRemoveTimestampedSnapshot(TestInfo testInfo)
            throws Exception
    {
        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;
        final String repositoryName = getRepositoryName(REPOSITORY_SNAPSHOTS, testInfo);
        final File repositoryBasedir = getRepositoryBasedir(STORAGE0, repositoryName);
        final String artifactPath = repositoryBasedir +
                                    "/org/carlspring/strongbox/strongbox-timestamped-first";

        File file = new File(artifactPath, "2.0-SNAPSHOT");

        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            try
            {
                if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
                {
                    assertEquals(1, file.listFiles((dir, name) -> name.endsWith(".jar")).length,
                                 "Amount of timestamped snapshots doesn't equal 1.");
                    assertTrue(getSnapshotArtifactVersion(repositoryBasedir, file).endsWith("-3"));
                }
            }
            catch (Exception e)
            {
                throw new UndeclaredThrowableException(e);
            }
        });

        addCronJobConfig(jobKey,
                         jobName,
                         RemoveTimestampedMavenSnapshotCronJob.class,
                         STORAGE0,
                         getRepositoryName(REPOSITORY_SNAPSHOTS,
                                           testInfo),
                         properties ->
                         {
                             properties.put("basePath", ARTIFACT_BASE_PATH_STRONGBOX_TIMESTAMPED);
                             properties.put("numberToKeep", "1");
                             properties.put("keepPeriod", "0");
                         });

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @Test
    public void testRemoveTimestampedSnapshotInRepository(TestInfo testInfo)
            throws Exception
    {
        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;
        final String repositoryName = getRepositoryName(REPOSITORY_SNAPSHOTS, testInfo);
        final File repositoryBasedir = getRepositoryBasedir(STORAGE0, repositoryName);
        final String artifactPath = repositoryBasedir +
                                    "/org/carlspring/strongbox/strongbox-timestamped-second";

        final File file = new File(artifactPath, "2.0-SNAPSHOT");

        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            try
            {
                if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
                {
                    assertEquals(1, file.listFiles((dir, name) -> name.endsWith(".jar")).length,
                                 "Amount of timestamped snapshots doesn't equal 1.");
                    assertTrue(getSnapshotArtifactVersion(repositoryBasedir, file).endsWith("-2"));
                }
            }
            catch (Exception e)
            {
                throw new UndeclaredThrowableException(e);
            }
        });

        addCronJobConfig(jobKey,
                         jobName,
                         RemoveTimestampedMavenSnapshotCronJob.class,
                         STORAGE0,
                         repositoryName,
                         properties ->
                         {
                             properties.put("basePath", null);
                             properties.put("numberToKeep", "1");
                             properties.put("keepPeriod", "0");
                         });

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    private String getSnapshotArtifactVersion(File repositoryBasedir,
                                              File artifactFile)
    {
        File[] files = artifactFile.listFiles((dir, name) -> name.endsWith(".jar"));
        Path path = files[0].toPath();
        Path other = repositoryBasedir.toPath();
        Path relativize = other.relativize(path);
        String unixBasedRelativePath = FilenameUtils.separatorsToUnix(relativize.toString());
        Artifact artifact = MavenArtifactUtils.convertPathToArtifact(unixBasedRelativePath);

        return artifact.getVersion();
    }

}
