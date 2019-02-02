package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.awaitility.Awaitility.await;
import static org.carlspring.strongbox.util.TestFileUtils.deleteIfExists;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
public class RegenerateMavenChecksumCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String REPOSITORY_SNAPSHOTS = "rmccj-snapshots";

    private MavenArtifact snapshotArtifact_1;

    private MavenArtifact snapshotArtifact_2;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);

        createRepository(STORAGE0,
                         getRepositoryName(REPOSITORY_SNAPSHOTS, testInfo),
                         RepositoryPolicyEnum.SNAPSHOT.getPolicy(),
                         false);

        snapshotArtifact_1 = createTimestampedSnapshotArtifact(
                getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath(),
                "org.carlspring.strongbox",
                "strongbox-checksum-one",
                "2.0",
                "jar",
                null,
                1);
        snapshotArtifact_2 = createTimestampedSnapshotArtifact(
                getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath(),
                "org.carlspring.strongbox",
                "strongbox-checksum-second",
                "2.0",
                "jar",
                null,
                1);
    }


    private Set<MutableRepository> getRepositories(TestInfo testInfo)
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(REPOSITORY_SNAPSHOTS, testInfo),
                                              Maven2LayoutProvider.ALIAS));
        return repositories;
    }

    @AfterEach
    public void removeRepositories(TestInfo testInfo)
            throws Exception
    {
        removeRepositories(getRepositories(testInfo));
    }

    @Test
    public void testRegenerateArtifactChecksum(TestInfo testInfo)
            throws Exception
    {
        final String jobName = expectedJobName;

        String artifactPath = getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath() +
                              "/org/carlspring/strongbox/strongbox-checksum-one";

        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                getRepositoryName(REPOSITORY_SNAPSHOTS, testInfo),
                                                "org/carlspring/strongbox/strongbox-checksum-one");

        deleteIfExists(new File(snapshotArtifact_1.getPath().toString() + ".md5"));
        deleteIfExists(new File(snapshotArtifact_1.getPath().toString() + ".sha1"));
        deleteIfExists(new File(snapshotArtifact_1.getPath().toString().replaceAll("jar", "pom") + ".md5"));
        deleteIfExists(new File(snapshotArtifact_1.getPath().toString().replaceAll("jar", "pom") + ".sha1"));

        deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/maven-metadata.xml.md5"));
        deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/maven-metadata.xml.sha1"));

        deleteIfExists(new File(artifactPath, "/maven-metadata.xml.md5"));
        deleteIfExists(new File(artifactPath, "/maven-metadata.xml.sha1"));

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {

                try
                {
                    assertTrue(new File(snapshotArtifact_1.getPath().toString() + ".sha1").exists(),
                               "The checksum file for artifact doesn't exist!");
                    assertTrue(new File(snapshotArtifact_1.getPath().toString() + ".sha1").length() > 0,
                               "The checksum file for artifact is empty!");

                    assertTrue(new File(snapshotArtifact_1.getPath().toString() + ".md5").exists(),
                               "The checksum file for artifact doesn't exist!");
                    assertTrue(new File(snapshotArtifact_1.getPath().toString() + ".md5").length() > 0,
                               "The checksum file for artifact is empty!");

                    assertTrue(new File(snapshotArtifact_1.getPath().toString().replaceAll("jar", "pom") +
                                        ".sha1").exists(),
                               "The checksum file for pom file doesn't exist!");
                    assertTrue(new File(snapshotArtifact_1.getPath().toString().replaceAll("jar", "pom") +
                                        ".md5").length() > 0,
                               "The checksum file for pom file is empty!");

                    assertTrue(new File(artifactPath, "/maven-metadata.xml.md5").exists(),
                               "The checksum file for metadata file doesn't exist!");
                    assertTrue(new File(artifactPath, "/maven-metadata.xml.sha1").length() > 0,
                               "The checksum file for metadata file is empty!");
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobName, RegenerateChecksumCronJob.class, STORAGE0,
                         getRepositoryName(REPOSITORY_SNAPSHOTS, testInfo),
                         properties ->
                         {
                             properties.put("basePath", "org/carlspring/strongbox/strongbox-checksum-one");
                             properties.put("forceRegeneration", "false");
                         });

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @Test
    public void testRegenerateChecksumInRepository(TestInfo testInfo)
            throws Exception
    {
        final String jobName = expectedJobName;

        String artifactPath = getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath() +
                              "/org/carlspring/strongbox/strongbox-checksum-second";

        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                getRepositoryName(REPOSITORY_SNAPSHOTS, testInfo),
                                                "org/carlspring/strongbox/strongbox-checksum-second");

        deleteIfExists(new File(snapshotArtifact_2.getPath().toString() + ".md5"));
        deleteIfExists(new File(snapshotArtifact_2.getPath().toString() + ".sha1"));
        deleteIfExists(new File(snapshotArtifact_2.getPath().toString().replaceAll("jar", "pom") + ".md5"));
        deleteIfExists(new File(snapshotArtifact_2.getPath().toString().replaceAll("jar", "pom") + ".sha1"));

        deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/maven-metadata.xml.md5"));
        deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/maven-metadata.xml.sha1"));

        deleteIfExists(new File(artifactPath, "/maven-metadata.xml.md5"));
        deleteIfExists(new File(artifactPath, "/maven-metadata.xml.sha1"));

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    assertTrue(new File(snapshotArtifact_2.getPath().toString() + ".sha1").exists(),
                               "The checksum file for artifact doesn't exist!");
                    assertTrue(new File(snapshotArtifact_2.getPath().toString() + ".sha1").length() > 0,
                               "The checksum file for artifact is empty!");

                    assertTrue(new File(snapshotArtifact_2.getPath().toString() + ".md5").exists(),
                               "The checksum file for artifact doesn't exist!");
                    assertTrue(new File(snapshotArtifact_2.getPath().toString() + ".md5").length() > 0,
                               "The checksum file for artifact is empty!");

                    assertTrue(new File(snapshotArtifact_2.getPath().toString().replaceAll("jar", "pom") +
                                        ".sha1").exists(),
                               "The checksum file for pom file doesn't exist!");
                    assertTrue(new File(snapshotArtifact_2.getPath().toString().replaceAll("jar", "pom") +
                                        ".sha1").length() > 0,
                               "The checksum file for pom file is empty!");

                    assertTrue(new File(artifactPath, "/maven-metadata.xml.md5").exists(),
                               "The checksum file for metadata file doesn't exist!");
                    assertTrue(new File(artifactPath, "/maven-metadata.xml.sha1").length() > 0,
                               "The checksum file for metadata file is empty!");
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobName,
                         RegenerateChecksumCronJob.class,
                         STORAGE0,
                         getRepositoryName(REPOSITORY_SNAPSHOTS, testInfo),
                         properties -> properties.put("forceRegeneration", "false"));

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

}
