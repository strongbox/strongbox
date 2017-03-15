package org.carlspring.strongbox.cron;

import org.carlspring.maven.commons.io.filters.JarFilenameFilter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.cron.api.jobs.RemoveTimestampedMavenSnapshotCronJob;
import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;

import javax.inject.Inject;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author Kate Novik.
 */
@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RemoveTimestampedMavenSnapshotCronJobTest
        extends TestCaseWithArtifactGeneration
{

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private JobManager jobManager;

    private static final File REPOSITORY_BASEDIR_1 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage0/snapshots");

    private static final File REPOSITORY_BASEDIR_2 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage0/snapshots-test");

    private static final File REPOSITORY_BASEDIR_3 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage1/snapshots");

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_TIMESTAMPED = "org/carlspring/strongbox/strongbox-timestamped-first";

    private DateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");

    private static boolean initialized;


    @Before
    public void setUp()
            throws Exception
    {
        if (!initialized)
        {
            //noinspection ResultOfMethodCallIgnored
            REPOSITORY_BASEDIR_1.mkdirs();
            REPOSITORY_BASEDIR_2.mkdirs();
            REPOSITORY_BASEDIR_3.mkdirs();

            //Create snapshot artifact in repository snapshots
            createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_1.getAbsolutePath(),
                                              "org.carlspring.strongbox",
                                              "strongbox-timestamped-first",
                                              "2.0",
                                              "jar",
                                              null,
                                              3);

            //Create snapshot artifact in repository snapshots
            createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_1.getAbsolutePath(),
                                              "org.carlspring.strongbox",
                                              "strongbox-timestamped-second",
                                              "2.0",
                                              "jar",
                                              null,
                                              2);

            //Create repository snapshots-test in storage0
            Storage storage = configurationManagementService.getStorage("storage0");
            Repository repository = new Repository("snapshots-test");
            repository.setPolicy(RepositoryPolicyEnum.SNAPSHOT.getPolicy());
            repository.setStorage(storage);
            repositoryManagementService.createRepository("storage0", "snapshots-test");
            storage.saveRepository(repository);

            createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_2.getAbsolutePath(),
                                              "org.carlspring.strongbox",
                                              "strongbox-timestamped-first",
                                              "2.0",
                                              "jar",
                                              null,
                                              5);

            //Create storage and repository for testing rebuild metadata in storages
            Storage storage1 = new Storage("storage1");
            Repository repository1 = new Repository("snapshots");
            repository1.setPolicy(RepositoryPolicyEnum.SNAPSHOT.getPolicy());
            repository1.setStorage(storage1);
            configurationManagementService.saveStorage(storage1);
            repositoryManagementService.createRepository("storage1", "snapshots");
            storage1.saveRepository(repository1);

            createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_3.getAbsolutePath(),
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

            createTimestampedSnapshot(REPOSITORY_BASEDIR_3.getAbsolutePath(),
                                      "org.carlspring.strongbox",
                                      "strongbox-timestamped-first",
                                      "2.0",
                                      "jar",
                                      null,
                                      2,
                                      timestamp);

            initialized = true;
        }
    }

    public void addRemoveCronJobConfig(String name,
                                       String storageId,
                                       String repositoryId,
                                       String basePath,
                                       int numberToKeep,
                                       int keepPeriod)
            throws Exception
    {
        CronTaskConfiguration cronTaskConfiguration = new CronTaskConfiguration();
        cronTaskConfiguration.setName(name);
        cronTaskConfiguration.addProperty("jobClass", RemoveTimestampedMavenSnapshotCronJob.class.getName());
        cronTaskConfiguration.addProperty("cronExpression", "0 0/1 * 1/1 * ? *");
        cronTaskConfiguration.addProperty("storageId", storageId);
        cronTaskConfiguration.addProperty("repositoryId", repositoryId);
        cronTaskConfiguration.addProperty("basePath", basePath);
        cronTaskConfiguration.addProperty("numberToKeep", String.valueOf(numberToKeep));
        cronTaskConfiguration.addProperty("keepPeriod", String.valueOf(keepPeriod));

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);
        CronTaskConfiguration obj = cronTaskConfigurationService.findOne(name);
        assertNotNull(obj);
    }

    public void deleteRemoveCronJobConfig(String name)
            throws Exception
    {
        List<CronTaskConfiguration> confs = cronTaskConfigurationService.getConfiguration(name);

        for (CronTaskConfiguration cnf : confs)
        {
            assertNotNull(cnf);
            cronTaskConfigurationService.deleteConfiguration(cnf);
        }

        assertNull(cronTaskConfigurationService.findOne(name));
    }

    @Test
    public void testRemoveTimestampedSnapshot()
            throws Exception
    {
        String jobName = "RemoveSnapshot-1";

        String artifactPath = REPOSITORY_BASEDIR_1 + "/org/carlspring/strongbox/strongbox-timestamped-first";

        File file = new File(artifactPath, "2.0-SNAPSHOT");

        artifactMetadataService.rebuildMetadata("storage0", "snapshots", ARTIFACT_BASE_PATH_STRONGBOX_TIMESTAMPED);


        addRemoveCronJobConfig(jobName, "storage0", "snapshots", ARTIFACT_BASE_PATH_STRONGBOX_TIMESTAMPED, 1, 0);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        System.out.println(jobManager.getExecutedJobs()
                                     .toString());

        assertEquals("Amount of timestamped snapshots doesn't equal 1.", 1,
                     file.listFiles(new JarFilenameFilter()).length);
        assertTrue(getSnapshotArtifactVersion(file).endsWith("-3"));

        deleteRemoveCronJobConfig(jobName);
    }

    @Test
    public void testRemoveTimestampedSnapshotInRepository()
            throws Exception
    {
        String jobName = "RemoveSnapshot-2";

        String artifactPath = REPOSITORY_BASEDIR_1 + "/org/carlspring/strongbox/strongbox-timestamped-second";

        File file = new File(artifactPath, "2.0-SNAPSHOT");

        artifactMetadataService.rebuildMetadata("storage0", "snapshots",
                                                "org/carlspring/strongbox/strongbox-timestamped-second");

        addRemoveCronJobConfig(jobName, "storage0", "snapshots", null, 1, 0);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        System.out.println(jobManager.getExecutedJobs()
                                     .toString());

        assertEquals("Amount of timestamped snapshots doesn't equal 1.", 1,
                     file.listFiles(new JarFilenameFilter()).length);
        assertTrue(getSnapshotArtifactVersion(file).endsWith("-2"));

        deleteRemoveCronJobConfig(jobName);
    }

    @Test
    public void testRemoveTimestampedSnapshotInStorage()
            throws Exception
    {
        String jobName = "RemoveSnapshot-3";

        String artifactPath = REPOSITORY_BASEDIR_2 + "/org/carlspring/strongbox/strongbox-timestamped-first";

        File file = new File(artifactPath, "2.0-SNAPSHOT");

        artifactMetadataService.rebuildMetadata("storage0", "snapshots-test",
                                                "org/carlspring/strongbox/strongbox-timestamped-first");

        addRemoveCronJobConfig(jobName, "storage0", null, null, 1, 0);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        assertEquals("Amount of timestamped snapshots doesn't equal 1.", 1,
                     file.listFiles(new JarFilenameFilter()).length);
        assertTrue(getSnapshotArtifactVersion(file).endsWith("-5"));

        deleteRemoveCronJobConfig(jobName);
    }

    @Test
    public void testRemoveTimestampedSnapshotInStorages()
            throws Exception
    {
        String jobName = "RemoveSnapshot-4";

        String artifactPath = REPOSITORY_BASEDIR_3 + "/org/carlspring/strongbox/strongbox-timestamped-first";

        File file = new File(artifactPath, "2.0-SNAPSHOT");

        artifactMetadataService.rebuildMetadata("storage1", "snapshots",
                                                "org/carlspring/strongbox/strongbox-timestamped-first");

        addRemoveCronJobConfig(jobName, null, null, null, 0, 3);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        assertEquals("Amount of timestamped snapshots doesn't equal 1.", 1,
                     file.listFiles(new JarFilenameFilter()).length);
        assertTrue(getSnapshotArtifactVersion(file).endsWith("-1"));

        deleteRemoveCronJobConfig(jobName);
    }

    private String getSnapshotArtifactVersion(File artifactFile)
    {
        File[] files = artifactFile.listFiles(new JarFilenameFilter());
        Artifact artifact = ArtifactUtils.convertPathToArtifact(files[0].getPath());

        return artifact.getVersion();
    }

}
