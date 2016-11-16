package org.carlspring.strongbox.cron;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.carlspring.strongbox.cron.api.jobs.RebuildMetadataCronJob;
import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.exceptions.CronTaskException;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
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
public class RebuildMetadataCronJobTest
        extends TestCaseWithArtifactGeneration {

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    private static final File REPOSITORY_BASEDIR_1 = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/snapshots");

    private static final File REPOSITORY_BASEDIR_2 = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/releases");

    private static final String[] CLASSIFIERS = { "javadoc", "sources", "source-release" };

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_METADATA = "org/carlspring/strongbox/strongbox-metadata-one";

    private static Artifact artifact1;

    private static Artifact artifact2;

    private static Artifact artifact3;

    private static boolean initialized;


    @Before
    public void setUp()
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        if (!initialized)
        {
            //noinspection ResultOfMethodCallIgnored
            REPOSITORY_BASEDIR_1.mkdirs();
            REPOSITORY_BASEDIR_2.mkdirs();


            // Create snapshot artifacts in repository snapshots
            artifact1 = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_1.getAbsolutePath(),
                    "org.carlspring.strongbox",
                    "strongbox-metadata-one",
                    "2.0",
                    "jar",
                    CLASSIFIERS,
                    5);

            // Create snapshot artifacts in repository snapshots
            artifact2 = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_1.getAbsolutePath(),
                    "org.carlspring.strongbox",
                    "strongbox-metadata-second",
                    "2.0",
                    "jar",
                    CLASSIFIERS,
                    5);

            // Create snapshot artifacts in repository snapshots-test-cron

            String ga = "org.carlspring.strongbox.metadata:strongbox-metadata";

            // Create released artifacts
                artifact3 = generateArtifact(REPOSITORY_BASEDIR_2.getAbsolutePath(), ga + ":1.0:jar");

//            artifact3 = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_2.getAbsolutePath(),
//                    "org.carlspring.strongbox",
//                    "strongbox-metadata-second",
//                    "2.0",
//                    "jar",
//                    CLASSIFIERS,
//                    5);

            changeCreationDate(artifact1);
            changeCreationDate(artifact2);
            changeCreationDate(artifact3);

            initialized = true;
        }
    }

    public void addRebuildCronJobConfig (String name, String storageId, String repositoryId, String basePath)
            throws ClassNotFoundException, CronTaskException,
            InstantiationException, SchedulerException,
            IllegalAccessException {

        CronTaskConfiguration cronTaskConfiguration = new CronTaskConfiguration();
        cronTaskConfiguration.setName(name);
        cronTaskConfiguration.addProperty("jobClass", RebuildMetadataCronJob.class.getName());
        cronTaskConfiguration.addProperty("cronExpression", "0 0/5 * 1/1 * ? *");
        cronTaskConfiguration.addProperty("storageId", storageId);
        cronTaskConfiguration.addProperty("repositoryId", repositoryId);
        cronTaskConfiguration.addProperty("basePath", basePath);

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);

        CronTaskConfiguration obj = cronTaskConfigurationService.findOne(name);
        assertNotNull(obj);
    }

    public void deleteRebuildCronJobConfig (String name)
            throws SchedulerException, CronTaskNotFoundException, ClassNotFoundException
    {
        List<CronTaskConfiguration> confs = cronTaskConfigurationService.getConfiguration(name);
        confs.forEach(cronTaskConfiguration ->
        {
            assertNotNull(cronTaskConfiguration);
            try
            {
                cronTaskConfigurationService.deleteConfiguration(
                        cronTaskConfiguration);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });

        assertNull(cronTaskConfigurationService.findOne(name));
    }

    @Test
    public void testRebuildArtifactsMetadata()
            throws NoSuchAlgorithmException, XmlPullParserException,
            IOException, ClassNotFoundException, SchedulerException,
            InstantiationException, CronTaskException, IllegalAccessException,
            CronTaskNotFoundException, InterruptedException {

        String jobName = "Rebuild-1";

        addRebuildCronJobConfig(jobName, "storage0", "snapshots", ARTIFACT_BASE_PATH_STRONGBOX_METADATA);

        Thread.sleep(50000);

        Metadata metadata = artifactMetadataService.getMetadata("storage0", "snapshots", "org/carlspring/strongbox/strongbox-metadata-one");

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
            throws NoSuchAlgorithmException, XmlPullParserException,
            IOException, ClassNotFoundException, SchedulerException,
            InstantiationException, CronTaskException, IllegalAccessException,
            CronTaskNotFoundException, InterruptedException {

        String jobName = "Rebuild-2";

        addRebuildCronJobConfig(jobName, "storage0", "snapshots", null);

        Thread.sleep(50000);

        Metadata metadata1 = artifactMetadataService.getMetadata("storage0", "snapshots", "org/carlspring/strongbox/strongbox-metadata-one");
        Metadata metadata2 = artifactMetadataService.getMetadata("storage0", "snapshots", "org/carlspring/strongbox/strongbox-metadata-second");

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

//    @Test
//    public void testRebuildMetadataInStorage()
//            throws NoSuchAlgorithmException, XmlPullParserException,
//            IOException, ClassNotFoundException, SchedulerException,
//            InstantiationException, CronTaskException, IllegalAccessException,
//            CronTaskNotFoundException, InterruptedException {
//
//        String jobName = "Rebuild-3";
//
//        addRebuildCronJobConfig(jobName, "storage0", null, null);
//
//        Thread.sleep(100000);
//
//        Metadata metadata1 = artifactMetadataService.getMetadata("storage0", "snapshots", "org/carlspring/strongbox/strongbox-metadata-one");
//        Metadata metadata2 = artifactMetadataService.getMetadata("storage0", "releases", "org/carlspring/strongbox/strongbox-metadata");
//
//        assertNotNull(metadata1);
//        assertNotNull(metadata2);
//
//        Versioning versioning1 = metadata1.getVersioning();
//        Versioning versioning2 = metadata1.getVersioning();
//
//        assertEquals("Incorrect artifactId!", artifact1.getArtifactId(), metadata1.getArtifactId());
//        assertEquals("Incorrect groupId!", artifact1.getGroupId(), metadata1.getGroupId());
//
//        assertEquals("Incorrect artifactId!", artifact3.getArtifactId(), metadata2.getArtifactId());
//        assertEquals("Incorrect groupId!", artifact3.getGroupId(), metadata2.getGroupId());
//
//        assertNotNull("No versioning information could be found in the metadata!", versioning1.getVersions().size());
//        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning1.getVersions().size());
//
//        assertNotNull("No versioning information could be found in the metadata!", versioning2.getVersions().size());
//        assertEquals("Incorrect number of versions stored in metadata!", 2, versioning2.getVersions().size());
//
//        deleteRebuildCronJobConfig(jobName);
//    }
}
