package org.carlspring.strongbox.cron.jobs;

import org.carlspring.maven.commons.io.filters.JarFilenameFilter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.maven.artifact.Artifact;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class RemoveTimestampedMavenSnapshotCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String STORAGE1 = "storage1";

    private static final String REPOSITORY_SNAPSHOTS_1 = "rtmscj-snapshots";

    private static final String REPOSITORY_SNAPSHOTS_2 = "rtmscj-snapshots-test";

    private static final File REPOSITORY_SNAPSHOTS_BASEDIR_1 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                        "/storages/" + STORAGE0 + "/" +
                                                                        REPOSITORY_SNAPSHOTS_1);

    private static final File REPOSITORY_SNAPSHOTS_BASEDIR_2 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                        "/storages/" + STORAGE0 + "/" +
                                                                        REPOSITORY_SNAPSHOTS_2);

    private static final File REPOSITORY_SNAPSHOTS_BASEDIR_3 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                        "/storages/" + STORAGE1 + "/" +
                                                                        REPOSITORY_SNAPSHOTS_1);

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_TIMESTAMPED = "org/carlspring/strongbox/strongbox-timestamped-first";

    private DateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_SNAPSHOTS_1, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_SNAPSHOTS_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_SNAPSHOTS_1, Maven2LayoutProvider.ALIAS));
        return repositories;
    }

    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);

        //Create repository rtmscj-snapshots in storage0
        createRepository(STORAGE0, REPOSITORY_SNAPSHOTS_1, RepositoryPolicyEnum.SNAPSHOT.getPolicy(), false);

        createTimestampedSnapshotArtifact(REPOSITORY_SNAPSHOTS_BASEDIR_1.getAbsolutePath(),
                                          "org.carlspring.strongbox",
                                          "strongbox-timestamped-first",
                                          "2.0",
                                          "jar",
                                          null,
                                          3);

        createTimestampedSnapshotArtifact(REPOSITORY_SNAPSHOTS_BASEDIR_1.getAbsolutePath(),
                                          "org.carlspring.strongbox",
                                          "strongbox-timestamped-second",
                                          "2.0",
                                          "jar",
                                          null,
                                          2);

        //Create repository rtmscj-snapshots-test in storage0
        createRepository(STORAGE0, REPOSITORY_SNAPSHOTS_2, RepositoryPolicyEnum.SNAPSHOT.getPolicy(), false);

        createTimestampedSnapshotArtifact(REPOSITORY_SNAPSHOTS_BASEDIR_2.getAbsolutePath(),
                                          "org.carlspring.strongbox",
                                          "strongbox-timestamped-first",
                                          "2.0",
                                          "jar",
                                          null,
                                          5);

        //Create storage and repository for testing removing timestamped snapshots in storages
        createStorage(STORAGE1);

        createRepository(STORAGE1, REPOSITORY_SNAPSHOTS_1, RepositoryPolicyEnum.SNAPSHOT.getPolicy(), false);

        createTimestampedSnapshotArtifact(REPOSITORY_SNAPSHOTS_BASEDIR_3.getAbsolutePath(),
                                          "org.carlspring.strongbox",
                                          "strongbox-timestamped-first",
                                          "2.0",
                                          "jar",
                                          null,
                                          1);

        //Creating timestamped snapshot with another timestamp
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -5);
        String timestamp = formatter.format(cal.getTime());

        createTimestampedSnapshot(REPOSITORY_SNAPSHOTS_BASEDIR_3.getAbsolutePath(),
                                  "org.carlspring.strongbox",
                                  "strongbox-timestamped-first",
                                  "2.0",
                                  "jar",
                                  null,
                                  2,
                                  timestamp);

        rebuildArtifactsMetadata();
    }

    @AfterEach
    public void removeRepositories()
            throws Exception
    {
        removeRepositories(getRepositoriesToClean());
        cleanUp();
    }

    private void rebuildArtifactsMetadata()
            throws Exception
    {
        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS_2,
                                                "org/carlspring/strongbox/strongbox-timestamped-first");

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS_1,
                                                "org/carlspring/strongbox/strongbox-timestamped-first");

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS_1,
                                                "org/carlspring/strongbox/strongbox-timestamped-second");
    }

    @Test
    public void testRemoveTimestampedSnapshot()
            throws Exception
    {
        final String jobName = expectedJobName;

        String artifactPath = REPOSITORY_SNAPSHOTS_BASEDIR_1 + "/org/carlspring/strongbox/strongbox-timestamped-first";

        File file = new File(artifactPath, "2.0-SNAPSHOT");

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            try
            {
                if (jobName.equals(jobName1) && statusExecuted)
                {
                    assertEquals(1, file.listFiles(new JarFilenameFilter()).length,
                                 "Amount of timestamped snapshots doesn't equal 1.");
                    assertTrue(getSnapshotArtifactVersion(file).endsWith("-3"));
                }
            }
            catch (Exception e)
            {
                throw new UndeclaredThrowableException(e);
            }
        });

        addCronJobConfig(jobName, RemoveTimestampedMavenSnapshotCronJob.class, STORAGE0, REPOSITORY_SNAPSHOTS_1,
                         properties ->
                         {
                             properties.put("basePath", ARTIFACT_BASE_PATH_STRONGBOX_TIMESTAMPED);
                             properties.put("numberToKeep", "1");
                             properties.put("keepPeriod", "0");
                         });

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @Test
    public void testRemoveTimestampedSnapshotInRepository()
            throws Exception
    {
        final String jobName = expectedJobName;

        String artifactPath = REPOSITORY_SNAPSHOTS_BASEDIR_1 + "/org/carlspring/strongbox/strongbox-timestamped-second";

        File file = new File(artifactPath, "2.0-SNAPSHOT");

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS_1,
                                                "org/carlspring/strongbox/strongbox-timestamped-first");

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS_1,
                                                "org/carlspring/strongbox/strongbox-timestamped-second");

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            try
            {
                if (jobName.equals(jobName1) && statusExecuted)
                {
                    assertEquals(1, file.listFiles(new JarFilenameFilter()).length,
                                 "Amount of timestamped snapshots doesn't equal 1.");
                    assertTrue(getSnapshotArtifactVersion(file).endsWith("-2"));
                }
            }
            catch (Exception e)
            {
                throw new UndeclaredThrowableException(e);
            }
        });

        addCronJobConfig(jobName, RemoveTimestampedMavenSnapshotCronJob.class, STORAGE0, REPOSITORY_SNAPSHOTS_1,
                         properties ->
                         {
                             properties.put("basePath", null);
                             properties.put("numberToKeep", "1");
                             properties.put("keepPeriod", "0");
                         });

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @Test
    public void testRemoveTimestampedSnapshotInStorage()
            throws Exception
    {
        final String jobName = expectedJobName;

        String artifactPath = REPOSITORY_SNAPSHOTS_BASEDIR_2 + "/org/carlspring/strongbox/strongbox-timestamped-first";

        File file = new File(artifactPath, "2.0-SNAPSHOT");

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS_2,
                                                "org/carlspring/strongbox/strongbox-timestamped-first");

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS_1,
                                                "org/carlspring/strongbox/strongbox-timestamped-first");

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS_1,
                                                "org/carlspring/strongbox/strongbox-timestamped-second");

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            try
            {
                if (jobName.equals(jobName1) && statusExecuted)
                {
                    assertEquals(1, file.listFiles(new JarFilenameFilter()).length,
                                 "Amount of timestamped snapshots doesn't equal 1.");
                    assertTrue(getSnapshotArtifactVersion(file).endsWith("-5"));
                }
            }
            catch (Exception e)
            {
                throw new UndeclaredThrowableException(e);
            }
        });

        addCronJobConfig(jobName, RemoveTimestampedMavenSnapshotCronJob.class, STORAGE0, null,
                         properties ->
                         {
                             properties.put("basePath", null);
                             properties.put("numberToKeep", "1");
                             properties.put("keepPeriod", "0");
                         });

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @Test
    public void testRemoveTimestampedSnapshotInStorages()
            throws Exception
    {
        final String jobName = expectedJobName;

        String artifactPath = REPOSITORY_SNAPSHOTS_BASEDIR_3 + "/org/carlspring/strongbox/strongbox-timestamped-first";

        File file = new File(artifactPath, "2.0-SNAPSHOT");

        artifactMetadataService.rebuildMetadata(STORAGE1, REPOSITORY_SNAPSHOTS_1,
                                                "org/carlspring/strongbox/strongbox-timestamped-first");

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            try
            {
                if (jobName.equals(jobName1) && statusExecuted)
                {
                    assertEquals(1, file.listFiles(new JarFilenameFilter()).length,
                                 "Amount of timestamped snapshots doesn't equal 1.");
                    assertTrue(getSnapshotArtifactVersion(file).endsWith("-1"));
                }
            }
            catch (Exception e)
            {
                throw new UndeclaredThrowableException(e);
            }
        });

        addCronJobConfig(jobName, RemoveTimestampedMavenSnapshotCronJob.class, null, null,
                         properties ->
                         {
                             properties.put("basePath", null);
                             properties.put("numberToKeep", "0");
                             properties.put("keepPeriod", "3");
                         });

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    private String getSnapshotArtifactVersion(File artifactFile)
    {
        File[] files = artifactFile.listFiles(new JarFilenameFilter());
        Artifact artifact = ArtifactUtils.convertPathToArtifact(files[0].getPath());

        return artifact.getVersion();
    }

}
