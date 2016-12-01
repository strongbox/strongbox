package org.carlspring.strongbox.cron;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;

import org.carlspring.strongbox.cron.api.jobs.RebuildMavenMetadataCronJob;
import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.exceptions.CronTaskException;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Kate Novik.
 */
@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RebuildMavenMetadataCronJobTest
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
                                                              "/storages/storage0/releases");

    private static final File REPOSITORY_BASEDIR_3 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage1/releases");

    private static final String[] CLASSIFIERS = { "javadoc",
                                                  "sources",
                                                  "source-release" };

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_METADATA = "org/carlspring/strongbox/strongbox-metadata-one";

    private static Artifact artifact1;

    private static Artifact artifact2;

    private static Artifact artifact3;

    private static Artifact artifact4;

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
            artifact1 = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_1.getAbsolutePath(),
                                                          "org.carlspring.strongbox",
                                                          "strongbox-metadata-one",
                                                          "2.0",
                                                          "jar",
                                                          CLASSIFIERS,
                                                          5);

            //Create snapshot artifact in repository snapshots
            artifact2 = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_1.getAbsolutePath(),
                                                          "org.carlspring.strongbox",
                                                          "strongbox-metadata-second",
                                                          "2.0",
                                                          "jar",
                                                          CLASSIFIERS,
                                                          5);

            //Create released artifact
            String ga = "org.carlspring.strongbox.metadata:strongbox-metadata";
            artifact3 = generateArtifact(REPOSITORY_BASEDIR_2.getAbsolutePath(), ga + ":1.0:jar");

            //Create storage and repository for testing rebuild metadata in storages
            Storage storage = new Storage("storage1");
            Repository repository = new Repository("releases");
            repository.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
            repository.setStorage(storage);
            configurationManagementService.addOrUpdateStorage(storage);
            repositoryManagementService.createRepository("storage1", "releases");
            storage.addOrUpdateRepository(repository);

            //Create released artifact
            artifact4 = generateArtifact(REPOSITORY_BASEDIR_3.getAbsolutePath(), ga + ":1.0:jar");

            changeCreationDate(artifact1);
            changeCreationDate(artifact2);
            changeCreationDate(artifact3);
            changeCreationDate(artifact4);

            initialized = true;
        }
    }

    public void addRebuildCronJobConfig(String name,
                                        String storageId,
                                        String repositoryId,
                                        String basePath)
            throws Exception
    {
        CronTaskConfiguration cronTaskConfiguration = new CronTaskConfiguration();
        cronTaskConfiguration.setName(name);
        cronTaskConfiguration.addProperty("jobClass", RebuildMavenMetadataCronJob.class.getName());
        cronTaskConfiguration.addProperty("cronExpression", "0 0/10 * 1/1 * ? *");
        cronTaskConfiguration.addProperty("storageId", storageId);
        cronTaskConfiguration.addProperty("repositoryId", repositoryId);
        cronTaskConfiguration.addProperty("basePath", basePath);

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);
        CronTaskConfiguration obj = cronTaskConfigurationService.findOne(name);
        assertNotNull(obj);
    }

    public void deleteRebuildCronJobConfig(String name)
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
    public void testRebuildArtifactsMetadata()
            throws Exception
    {
        String jobName = "Rebuild-1";

        addRebuildCronJobConfig(jobName, "storage0", "snapshots", ARTIFACT_BASE_PATH_STRONGBOX_METADATA);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs().containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        System.out.println(jobManager.getExecutedJobs().toString());

        Metadata metadata = artifactMetadataService.getMetadata("storage0", "snapshots",
                                                                "org/carlspring/strongbox/strongbox-metadata-one");

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals("Incorrect artifactId!", artifact1.getArtifactId(), metadata.getArtifactId());
        assertEquals("Incorrect groupId!", artifact1.getGroupId(), metadata.getGroupId());

        assertNotNull("No versioning information could be found in the metadata!", versioning.getVersions().size());
        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning.getVersions().size());

        deleteRebuildCronJobConfig(jobName);
    }

    @Test
    public void testRebuildMetadataInRepository()
            throws Exception
    {
        String jobName = "Rebuild-2";

        addRebuildCronJobConfig(jobName, "storage0", "snapshots", null);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs().containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        Metadata metadata1 = artifactMetadataService.getMetadata("storage0", "snapshots",
                                                                 "org/carlspring/strongbox/strongbox-metadata-one");
        Metadata metadata2 = artifactMetadataService.getMetadata("storage0", "snapshots",
                                                                 "org/carlspring/strongbox/strongbox-metadata-second");

        assertNotNull(metadata1);
        assertNotNull(metadata2);

        Versioning versioning1 = metadata1.getVersioning();
        Versioning versioning2 = metadata1.getVersioning();

        assertEquals("Incorrect artifactId!", artifact1.getArtifactId(), metadata1.getArtifactId());
        assertEquals("Incorrect groupId!", artifact1.getGroupId(), metadata1.getGroupId());

        assertEquals("Incorrect artifactId!", artifact2.getArtifactId(), metadata2.getArtifactId());
        assertEquals("Incorrect groupId!", artifact2.getGroupId(), metadata2.getGroupId());

        assertNotNull("No versioning information could be found in the metadata!", versioning1.getVersions().size());
        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning1.getVersions().size());

        assertNotNull("No versioning information could be found in the metadata!", versioning2.getVersions().size());
        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning2.getVersions().size());

        deleteRebuildCronJobConfig(jobName);
    }

    @Test
    public void testRebuildMetadataInStorage()
            throws Exception
    {
        String jobName = "Rebuild-3";

        addRebuildCronJobConfig(jobName, "storage0", null, null);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs().containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        Metadata metadata1 = artifactMetadataService.getMetadata("storage0", "snapshots",
                                                                 "org/carlspring/strongbox/strongbox-metadata-one");
        Metadata metadata2 = artifactMetadataService.getMetadata("storage0", "releases",
                                                                 "org/carlspring/strongbox/metadata/strongbox-metadata");

        assertNotNull(metadata1);
        assertNotNull(metadata2);

        Versioning versioning1 = metadata1.getVersioning();
        Versioning versioning2 = metadata1.getVersioning();

        assertEquals("Incorrect artifactId!", artifact1.getArtifactId(), metadata1.getArtifactId());
        assertEquals("Incorrect groupId!", artifact1.getGroupId(), metadata1.getGroupId());

        assertEquals("Incorrect artifactId!", artifact3.getArtifactId(), metadata2.getArtifactId());
        assertEquals("Incorrect groupId!", artifact3.getGroupId(), metadata2.getGroupId());

        assertNotNull("No versioning information could be found in the metadata!", versioning1.getVersions().size());
        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning1.getVersions().size());

        assertNotNull("No versioning information could be found in the metadata!", versioning2.getVersions().size());
        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning2.getVersions().size());

        deleteRebuildCronJobConfig(jobName);
    }

    @Test
    public void testRebuildMetadataInStorages()
            throws Exception
    {
        String jobName = "Rebuild-4";

        addRebuildCronJobConfig(jobName, null, null, null);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs().containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        Metadata metadata1 = artifactMetadataService.getMetadata("storage0", "snapshots",
                                                                 "org/carlspring/strongbox/strongbox-metadata-one");
        Metadata metadata2 = artifactMetadataService.getMetadata("storage1", "releases",
                                                                 "org/carlspring/strongbox/metadata/strongbox-metadata");

        assertNotNull(metadata1);
        assertNotNull(metadata2);

        Versioning versioning1 = metadata1.getVersioning();
        Versioning versioning2 = metadata1.getVersioning();

        assertEquals("Incorrect artifactId!", artifact1.getArtifactId(), metadata1.getArtifactId());
        assertEquals("Incorrect groupId!", artifact1.getGroupId(), metadata1.getGroupId());

        assertEquals("Incorrect artifactId!", artifact4.getArtifactId(), metadata2.getArtifactId());
        assertEquals("Incorrect groupId!", artifact4.getGroupId(), metadata2.getGroupId());

        assertNotNull("No versioning information could be found in the metadata!", versioning1.getVersions().size());
        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning1.getVersions().size());

        assertNotNull("No versioning information could be found in the metadata!", versioning2.getVersions().size());
        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning2.getVersions().size());

        deleteRebuildCronJobConfig(jobName);
    }
}
