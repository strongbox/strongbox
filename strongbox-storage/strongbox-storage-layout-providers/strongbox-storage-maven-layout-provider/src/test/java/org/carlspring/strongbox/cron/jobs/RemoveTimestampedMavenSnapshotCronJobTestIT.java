package org.carlspring.strongbox.cron.jobs;

import org.carlspring.maven.commons.io.filters.JarFilenameFilter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class RemoveTimestampedMavenSnapshotCronJobTestIT
        extends TestCaseWithMavenArtifactGenerationAndIndexing
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

                    deleteRemoveCronJobConfig(jobName);
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });

        addRemoveCronJobConfig(jobName,
                               STORAGE0,
                               REPOSITORY_SNAPSHOTS_1,
                               ARTIFACT_BASE_PATH_STRONGBOX_TIMESTAMPED,
                               1,
                               0);

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

                    deleteRemoveCronJobConfig(jobName);
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });

        addRemoveCronJobConfig(jobName, STORAGE0, REPOSITORY_SNAPSHOTS_1, null, 1, 0);
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

                    deleteRemoveCronJobConfig(jobName);
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });

        addRemoveCronJobConfig(jobName, STORAGE0, null, null, 1, 0);
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

                    deleteRemoveCronJobConfig(jobName);
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });

        addRemoveCronJobConfig(jobName, null, null, null, 0, 3);
    }

    private String getSnapshotArtifactVersion(File artifactFile)
    {
        File[] files = artifactFile.listFiles(new JarFilenameFilter());
        Artifact artifact = ArtifactUtils.convertPathToArtifact(files[0].getPath());

        return artifact.getVersion();
    }

}
