package org.carlspring.strongbox.cron.jobs;

import org.carlspring.maven.commons.io.filters.JarFilenameFilter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.event.cron.CronTaskEventTypeEnum;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
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
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private JobManager jobManager;

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Before
    public void initialize()
            throws Exception
    {
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
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_SNAPSHOTS_1));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_SNAPSHOTS_2));
        repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_SNAPSHOTS_1));
        return repositories;
    }

    @Test
    public void testRemoveTimestampedSnapshot()
            throws Exception
    {
        String jobName = "RemoveSnapshot-1";

        String artifactPath = REPOSITORY_SNAPSHOTS_BASEDIR_1 + "/org/carlspring/strongbox/strongbox-timestamped-first";

        File file = new File(artifactPath, "2.0-SNAPSHOT");

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS_1,
                                                ARTIFACT_BASE_PATH_STRONGBOX_TIMESTAMPED);

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            try
            {
                if (jobName.equals(jobName1) && statusExecuted)
                {
                    assertEquals("Amount of timestamped snapshots doesn't equal 1.", 1,
                                 file.listFiles(new JarFilenameFilter()).length);
                    assertTrue(getSnapshotArtifactVersion(file).endsWith("-3"));
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });

        addCronJobConfig(jobName, RemoveTimestampedMavenSnapshotCronJob.class, STORAGE0, REPOSITORY_SNAPSHOTS_1,
                         properties ->
                         {
                             properties.put("basePath", ARTIFACT_BASE_PATH_STRONGBOX_TIMESTAMPED);
                             properties.put("numberToKeep", "1");
                             properties.put("keepPeriod", "0");
                         });

        assertTrue("Failed to execute task!",
                   expectEvent(CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType()));
    }

    @Test
    public void testRemoveTimestampedSnapshotInRepository()
            throws Exception
    {
        String jobName = "RemoveSnapshot-2";

        String artifactPath = REPOSITORY_SNAPSHOTS_BASEDIR_1 + "/org/carlspring/strongbox/strongbox-timestamped-second";

        File file = new File(artifactPath, "2.0-SNAPSHOT");

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS_1,
                                                "org/carlspring/strongbox/strongbox-timestamped-second");

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            try
            {
                if (jobName.equals(jobName1) && statusExecuted)
                {
                    assertEquals("Amount of timestamped snapshots doesn't equal 1.", 1,
                                 file.listFiles(new JarFilenameFilter()).length);
                    assertTrue(getSnapshotArtifactVersion(file).endsWith("-2"));
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });

        addCronJobConfig(jobName, RemoveTimestampedMavenSnapshotCronJob.class, STORAGE0, REPOSITORY_SNAPSHOTS_1,
                         properties ->
                         {
                             properties.put("basePath", null);
                             properties.put("numberToKeep", "1");
                             properties.put("keepPeriod", "0");
                         });

        assertTrue("Failed to execute task!",
                   expectEvent(CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType()));
    }

    @Test
    public void testRemoveTimestampedSnapshotInStorage()
            throws Exception
    {
        String jobName = "RemoveSnapshot-3";

        String artifactPath = REPOSITORY_SNAPSHOTS_BASEDIR_2 + "/org/carlspring/strongbox/strongbox-timestamped-first";

        File file = new File(artifactPath, "2.0-SNAPSHOT");

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS_2,
                                                "org/carlspring/strongbox/strongbox-timestamped-first");

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            try
            {
                if (jobName.equals(jobName1) && statusExecuted)
                {
                    assertEquals("Amount of timestamped snapshots doesn't equal 1.", 1,
                                 file.listFiles(new JarFilenameFilter()).length);
                    assertTrue(getSnapshotArtifactVersion(file).endsWith("-5"));
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });

        addCronJobConfig(jobName, RemoveTimestampedMavenSnapshotCronJob.class, STORAGE0, null,
                         properties ->
                         {
                             properties.put("basePath", null);
                             properties.put("numberToKeep", "1");
                             properties.put("keepPeriod", "0");
                         });

        assertTrue("Failed to execute task!",
                   expectEvent(CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType()));
    }

    @Test
    public void testRemoveTimestampedSnapshotInStorages()
            throws Exception
    {
        String jobName = "RemoveSnapshot-4";

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
                    assertEquals("Amount of timestamped snapshots doesn't equal 1.", 1,
                                 file.listFiles(new JarFilenameFilter()).length);
                    assertTrue(getSnapshotArtifactVersion(file).endsWith("-1"));
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });

        addCronJobConfig(jobName, RemoveTimestampedMavenSnapshotCronJob.class, null, null,
                         properties ->
                         {
                             properties.put("basePath", null);
                             properties.put("numberToKeep", "0");
                             properties.put("keepPeriod", "3");
                         });

        assertTrue("Failed to execute task!",
                   expectEvent(CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType()));
    }

    private String getSnapshotArtifactVersion(File artifactFile)
    {
        File[] files = artifactFile.listFiles(new JarFilenameFilter());
        Artifact artifact = ArtifactUtils.convertPathToArtifact(files[0].getPath());

        return artifact.getVersion();
    }

}
