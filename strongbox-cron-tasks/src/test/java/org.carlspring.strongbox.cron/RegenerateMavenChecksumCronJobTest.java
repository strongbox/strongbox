package org.carlspring.strongbox.cron;

import org.carlspring.maven.commons.io.filters.JarFilenameFilter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.cron.api.jobs.RegenerateChecksumCronJob;
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
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGeneration;
import org.carlspring.strongbox.util.FileUtils;

import javax.inject.Inject;
import java.io.File;
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
public class RegenerateMavenChecksumCronJobTest
        extends TestCaseWithMavenArtifactGeneration
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
                                                              "/storages/storage0/releases");

    private static final File REPOSITORY_BASEDIR_3 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage1/releases");

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_CHECKSUM = "org/carlspring/strongbox/strongbox-checksum-one";

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
                                              "strongbox-checksum-one",
                                              "2.0",
                                              "jar",
                                              null,
                                              1);

            //Create snapshot artifact in repository snapshots
            createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_1.getAbsolutePath(),
                                              "org.carlspring.strongbox",
                                              "strongbox-checksum-second",
                                              "2.0",
                                              "jar",
                                              null,
                                              1);

            //Create released artifact
            String ga = "org.carlspring.strongbox.checksum:strongbox-checksum";
            generateArtifact(REPOSITORY_BASEDIR_2.getAbsolutePath(), ga + ":1.0:jar");

            //Create storage and repository for testing rebuild metadata in storages
            Storage storage = new Storage("storage1");
            Repository repository = new Repository("releases");
            repository.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
            repository.setStorage(storage);
            configurationManagementService.saveStorage(storage);
            storage.addRepository(repository);
            repositoryManagementService.createRepository("storage1", "releases");

            //Create released artifact
            generateArtifact(REPOSITORY_BASEDIR_3.getAbsolutePath(), ga + ":1.0:jar");

            initialized = true;
        }
    }

    public void addRegenerateCronJobConfig(String name,
                                           String storageId,
                                           String repositoryId,
                                           String basePath,
                                           boolean forceRegeneration)
            throws Exception
    {
        CronTaskConfiguration cronTaskConfiguration = new CronTaskConfiguration();
        cronTaskConfiguration.setName(name);
        cronTaskConfiguration.addProperty("jobClass", RegenerateChecksumCronJob.class.getName());
        cronTaskConfiguration.addProperty("cronExpression", "0 0/1 * 1/1 * ? *");
        cronTaskConfiguration.addProperty("storageId", storageId);
        cronTaskConfiguration.addProperty("repositoryId", repositoryId);
        cronTaskConfiguration.addProperty("basePath", basePath);
        cronTaskConfiguration.addProperty("forceRegeneration", String.valueOf(forceRegeneration));

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);
        CronTaskConfiguration obj = cronTaskConfigurationService.findOne(name);
        assertNotNull(obj);
    }

    public void deleteRegenerateCronJobConfig(String name)
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
    public void testRegenerateArtifactChecksum()
            throws Exception
    {
        String jobName = "Regenerate-1";

        String artifactPath = REPOSITORY_BASEDIR_1 + "/org/carlspring/strongbox/strongbox-checksum-one";

        String artifactVersion = getSnapshotArtifactVersion(artifactPath.concat("/2.0-SNAPSHOT"));

        artifactMetadataService.rebuildMetadata("storage0", "snapshots", ARTIFACT_BASE_PATH_STRONGBOX_CHECKSUM);

        FileUtils.deleteIfExists(
                new File(artifactPath, "/2.0-SNAPSHOT/strongbox-checksum-one-" + artifactVersion + ".jar.md5"));
        FileUtils.deleteIfExists(
                new File(artifactPath, "/2.0-SNAPSHOT/strongbox-checksum-one-" + artifactVersion + ".jar.sha1"));
        FileUtils.deleteIfExists(
                new File(artifactPath, "/2.0-SNAPSHOT/strongbox-checksum-one-" + artifactVersion + ".pom.md5"));
        FileUtils.deleteIfExists(
                new File(artifactPath, "/2.0-SNAPSHOT/strongbox-checksum-one-" + artifactVersion + ".pom.sha1"));

        FileUtils.deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/maven-metadata.xml.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/maven-metadata.xml.sha1"));

        FileUtils.deleteIfExists(new File(artifactPath, "/maven-metadata.xml.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/maven-metadata.xml.sha1"));

        assertTrue("The checksum file for artifact exist!",
                   !new File(artifactPath,
                             "/2.0-SNAPSHOT/strongbox-checksum-one-" + artifactVersion + ".jar.md5").exists());

        addRegenerateCronJobConfig(jobName, "storage0", "snapshots", ARTIFACT_BASE_PATH_STRONGBOX_CHECKSUM, false);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        System.out.println(jobManager.getExecutedJobs()
                                     .toString());

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath,
                            "/2.0-SNAPSHOT/strongbox-checksum-one-" + artifactVersion + ".jar.sha1").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath,
                            "/2.0-SNAPSHOT/strongbox-checksum-one-" + artifactVersion + ".jar.sha1").length() > 0);

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath,
                            "/2.0-SNAPSHOT/strongbox-checksum-one-" + artifactVersion + ".jar.md5").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath,
                            "/2.0-SNAPSHOT/strongbox-checksum-one-" + artifactVersion + ".jar.md5").length() > 0);

        assertTrue("The checksum file for pom file doesn't exist!",
                   new File(artifactPath,
                            "/2.0-SNAPSHOT/strongbox-checksum-one-" + artifactVersion + ".pom.sha1").exists());
        assertTrue("The checksum file for pom file is empty!",
                   new File(artifactPath,
                            "/2.0-SNAPSHOT/strongbox-checksum-one-" + artifactVersion + ".pom.md5").length() > 0);

        assertTrue("The checksum file for metadata file doesn't exist!",
                   new File(artifactPath, "/maven-metadata.xml.md5").exists());
        assertTrue("The checksum file for metadata file is empty!",
                   new File(artifactPath, "/maven-metadata.xml.sha1").length() > 0);

        deleteRegenerateCronJobConfig(jobName);
    }

    @Test
    public void testRegenerateChecksumInRepository()
            throws Exception
    {
        String jobName = "Regenerate-2";

        String artifactPath = REPOSITORY_BASEDIR_1 + "/org/carlspring/strongbox/strongbox-checksum-second";

        String artifactVersion = getSnapshotArtifactVersion(artifactPath.concat("/2.0-SNAPSHOT"));

        artifactMetadataService.rebuildMetadata("storage0", "snapshots",
                                                "org/carlspring/strongbox/strongbox-checksum-second");

        FileUtils.deleteIfExists(
                new File(artifactPath, "/2.0-SNAPSHOT/strongbox-checksum-second-" + artifactVersion + ".jar.md5"));
        FileUtils.deleteIfExists(
                new File(artifactPath, "/2.0-SNAPSHOT/strongbox-checksum-second-" + artifactVersion + ".jar.sha1"));
        FileUtils.deleteIfExists(
                new File(artifactPath, "/2.0-SNAPSHOT/strongbox-checksum-second-" + artifactVersion + ".pom.md5"));
        FileUtils.deleteIfExists(
                new File(artifactPath, "/2.0-SNAPSHOT/strongbox-checksum-second-" + artifactVersion + ".pom.sha1"));

        FileUtils.deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/maven-metadata.xml.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/maven-metadata.xml.sha1"));

        FileUtils.deleteIfExists(new File(artifactPath, "/maven-metadata.xml.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/maven-metadata.xml.sha1"));

        assertTrue("The checksum file for artifact exist!",
                   !new File(artifactPath,
                             "/2.0-SNAPSHOT/strongbox-checksum-second-" + artifactVersion + ".jar.md5").exists());

        addRegenerateCronJobConfig(jobName, "storage0", "snapshots", null, false);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        System.out.println(jobManager.getExecutedJobs()
                                     .toString());

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath,
                            "/2.0-SNAPSHOT/strongbox-checksum-second-" + artifactVersion + ".jar.sha1").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath,
                            "/2.0-SNAPSHOT/strongbox-checksum-second-" + artifactVersion + ".jar.sha1").length() > 0);

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath,
                            "/2.0-SNAPSHOT/strongbox-checksum-second-" + artifactVersion + ".jar.md5").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath,
                            "/2.0-SNAPSHOT/strongbox-checksum-second-" + artifactVersion + ".jar.md5").length() > 0);

        assertTrue("The checksum file for pom file doesn't exist!",
                   new File(artifactPath,
                            "/2.0-SNAPSHOT/strongbox-checksum-second-" + artifactVersion + ".pom.sha1").exists());
        assertTrue("The checksum file for pom file is empty!",
                   new File(artifactPath,
                            "/2.0-SNAPSHOT/strongbox-checksum-second-" + artifactVersion + ".pom.md5").length() > 0);

        assertTrue("The checksum file for metadata file doesn't exist!",
                   new File(artifactPath, "/maven-metadata.xml.md5").exists());
        assertTrue("The checksum file for metadata file is empty!",
                   new File(artifactPath, "/maven-metadata.xml.sha1").length() > 0);

        deleteRegenerateCronJobConfig(jobName);
    }

    @Test
    public void testRegenerateChecksumInStorage()
            throws Exception
    {
        String jobName = "Regenerate-3";

        String artifactPath = REPOSITORY_BASEDIR_2 + "/org/carlspring/strongbox/checksum/strongbox-checksum";

        artifactMetadataService.rebuildMetadata("storage0", "releases", "org/carlspring/strongbox/checksum");

        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.sha1"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.sha1"));

        FileUtils.deleteIfExists(new File(artifactPath, "/maven-metadata.xml.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/maven-metadata.xml.sha1"));

        assertTrue("The checksum file for artifact exist!",
                   !new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5").exists());

        addRegenerateCronJobConfig(jobName, "storage0", null, null, false);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.sha1").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.sha1").length() > 0);

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5").length() > 0);

        assertTrue("The checksum file for pom file doesn't exist!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.sha1").exists());
        assertTrue("The checksum file for pom file is empty!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.md5").length() > 0);

        assertTrue("The checksum file for metadata file doesn't exist!",
                   new File(artifactPath, "/maven-metadata.xml.md5").exists());
        assertTrue("The checksum file for metadata file is empty!",
                   new File(artifactPath, "/maven-metadata.xml.sha1").length() > 0);

        deleteRegenerateCronJobConfig(jobName);
    }

    @Test
    public void testRegenerateChecksumInStorages()
            throws Exception
    {
        String jobName = "Regenerate-4";

        String artifactPath = REPOSITORY_BASEDIR_3 + "/org/carlspring/strongbox/checksum/strongbox-checksum";

        artifactMetadataService.rebuildMetadata("storage1", "releases", "org/carlspring/strongbox/checksum");

        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.sha1"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.sha1"));

        FileUtils.deleteIfExists(new File(artifactPath, "/maven-metadata.xml.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/maven-metadata.xml.sha1"));

        assertTrue("The checksum file for artifact exist!",
                   !new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5").exists());

        addRegenerateCronJobConfig(jobName, null, null, null, false);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.sha1").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.sha1").length() > 0);

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5").length() > 0);

        assertTrue("The checksum file for pom file doesn't exist!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.sha1").exists());
        assertTrue("The checksum file for pom file is empty!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.md5").length() > 0);

        assertTrue("The checksum file for metadata file doesn't exist!",
                   new File(artifactPath, "/maven-metadata.xml.md5").exists());
        assertTrue("The checksum file for metadata file is empty!",
                   new File(artifactPath, "/maven-metadata.xml.sha1").length() > 0);

        deleteRegenerateCronJobConfig(jobName);
    }

    private String getSnapshotArtifactVersion(String artifactPath)
    {
        File file = new File(artifactPath);
        File[] files = file.listFiles(new JarFilenameFilter());
        Artifact artifact = ArtifactUtils.convertPathToArtifact(files[0].getPath());
        return artifact.getVersion();
    }

}
