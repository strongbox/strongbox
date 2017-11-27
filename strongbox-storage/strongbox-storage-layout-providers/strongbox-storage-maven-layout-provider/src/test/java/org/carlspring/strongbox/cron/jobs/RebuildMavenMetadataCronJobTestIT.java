package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class RebuildMavenMetadataCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String STORAGE1 = "storage1";

    private static final String REPOSITORY_RELEASES = "rmmcj-releases";

    private static final String REPOSITORY_SNAPSHOTS = "rmmcj-snapshots";

    private static final File REPOSITORY_RELEASES_BASEDIR_1 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE0 + "/" +
                                                                       REPOSITORY_RELEASES);

    private static final File REPOSITORY_SNAPSHOTS_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                      "/storages/" + STORAGE0 + "/" +
                                                                      REPOSITORY_SNAPSHOTS);

    private static final File REPOSITORY_RELEASES_BASEDIR_2 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE1 + "/" +
                                                                       REPOSITORY_RELEASES);

    private static final String[] CLASSIFIERS = { "javadoc",
                                                  "sources",
                                                  "source-release" };

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_METADATA = "org/carlspring/strongbox/strongbox-metadata-one";

    private static Artifact artifact1;

    private static Artifact artifact2;

    private static Artifact artifact3;

    private static Artifact artifact4;

    @Rule
    public TestRule watcher = new TestWatcher()
    {
        @Override
        protected void starting(final Description description)
        {
            expectedJobName = description.getMethodName();
        }
    };

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_SNAPSHOTS));
        repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_RELEASES));

        return repositories;
    }

    @Before
    public void initialize()
            throws Exception
    {
        createRepository(STORAGE0, REPOSITORY_SNAPSHOTS, RepositoryPolicyEnum.SNAPSHOT.getPolicy(), false);

        artifact1 = createTimestampedSnapshotArtifact(REPOSITORY_SNAPSHOTS_BASEDIR.getAbsolutePath(),
                                                      "org.carlspring.strongbox",
                                                      "strongbox-metadata-one",
                                                      "2.0",
                                                      "jar",
                                                      CLASSIFIERS,
                                                      5);

        artifact2 = createTimestampedSnapshotArtifact(REPOSITORY_SNAPSHOTS_BASEDIR.getAbsolutePath(),
                                                      "org.carlspring.strongbox",
                                                      "strongbox-metadata-second",
                                                      "2.0",
                                                      "jar",
                                                      CLASSIFIERS,
                                                      5);

        createRepository(STORAGE0, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy(), false);

        artifact3 = generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                                     "org.carlspring.strongbox.metadata:strongbox-metadata:1.0:jar");

        createStorage(STORAGE1);

        createRepository(STORAGE1, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy(), false);

        artifact4 = generateArtifact(REPOSITORY_RELEASES_BASEDIR_2.getAbsolutePath(),
                                     "org.carlspring.strongbox.metadata:strongbox-metadata:1.0:jar");
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());

    }

    @Test
    public void testRebuildArtifactsMetadata()
            throws Exception
    {
        String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    Metadata metadata = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS,
                                                                            "org/carlspring/strongbox/strongbox-metadata-one");

                    assertNotNull(metadata);

                    Versioning versioning = metadata.getVersioning();

                    assertEquals("Incorrect artifactId!", artifact1.getArtifactId(), metadata.getArtifactId());
                    assertEquals("Incorrect groupId!", artifact1.getGroupId(), metadata.getGroupId());

                    assertNotNull("No versioning information could be found in the metadata!", versioning.getVersions()
                                                                                                         .size());
                    assertEquals("Incorrect number of versions stored in metadata!", 1, versioning.getVersions()
                                                                                                  .size());
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, RebuildMavenMetadataCronJob.class, STORAGE0, REPOSITORY_SNAPSHOTS,
                         properties -> properties.put("basePath", ARTIFACT_BASE_PATH_STRONGBOX_METADATA));

        assertTrue("Failed to execute task!", expectEvent());
    }

    @Test
    public void testRebuildMetadataInRepository()
            throws Exception
    {
        String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    Metadata metadata1 = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS,
                                                                             "org/carlspring/strongbox/strongbox-metadata-one");
                    Metadata metadata2 = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS,
                                                                             "org/carlspring/strongbox/strongbox-metadata-second");

                    assertNotNull(metadata1);
                    assertNotNull(metadata2);

                    Versioning versioning1 = metadata1.getVersioning();
                    Versioning versioning2 = metadata1.getVersioning();

                    assertEquals("Incorrect artifactId!", artifact1.getArtifactId(), metadata1.getArtifactId());
                    assertEquals("Incorrect groupId!", artifact1.getGroupId(), metadata1.getGroupId());

                    assertEquals("Incorrect artifactId!", artifact2.getArtifactId(), metadata2.getArtifactId());
                    assertEquals("Incorrect groupId!", artifact2.getGroupId(), metadata2.getGroupId());

                    assertNotNull("No versioning information could be found in the metadata!", versioning1.getVersions()
                                                                                                          .size());
                    assertEquals("Incorrect number of versions stored in metadata!", 1, versioning1.getVersions()
                                                                                                   .size());

                    assertNotNull("No versioning information could be found in the metadata!", versioning2.getVersions()
                                                                                                          .size());
                    assertEquals("Incorrect number of versions stored in metadata!", 1, versioning2.getVersions()
                                                                                                   .size());
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, RebuildMavenMetadataCronJob.class, STORAGE0, REPOSITORY_SNAPSHOTS);

        assertTrue("Failed to execute task!", expectEvent());
    }

    @Test
    public void testRebuildMetadataInStorage()
            throws Exception
    {
        String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    Metadata metadata1 = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS,
                                                                             "org/carlspring/strongbox/strongbox-metadata-one");
                    Metadata metadata2 = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_RELEASES,
                                                                             "org/carlspring/strongbox/metadata/strongbox-metadata");

                    assertNotNull(metadata1);
                    assertNotNull(metadata2);

                    Versioning versioning1 = metadata1.getVersioning();
                    Versioning versioning2 = metadata1.getVersioning();

                    assertEquals("Incorrect artifactId!", artifact1.getArtifactId(), metadata1.getArtifactId());
                    assertEquals("Incorrect groupId!", artifact1.getGroupId(), metadata1.getGroupId());

                    assertEquals("Incorrect artifactId!", artifact3.getArtifactId(), metadata2.getArtifactId());
                    assertEquals("Incorrect groupId!", artifact3.getGroupId(), metadata2.getGroupId());

                    assertNotNull("No versioning information could be found in the metadata!", versioning1.getVersions()
                                                                                                          .size());
                    assertEquals("Incorrect number of versions stored in metadata!", 1, versioning1.getVersions()
                                                                                                   .size());

                    assertNotNull("No versioning information could be found in the metadata!", versioning2.getVersions()
                                                                                                          .size());
                    assertEquals("Incorrect number of versions stored in metadata!", 1, versioning2.getVersions()
                                                                                                   .size());
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, RebuildMavenMetadataCronJob.class, STORAGE0, null);

        assertTrue("Failed to execute task!", expectEvent());
    }

    @Test
    public void testRebuildMetadataInStorages()
            throws Exception
    {
        String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    Metadata metadata1 = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS,
                                                                             "org/carlspring/strongbox/strongbox-metadata-one");
                    Metadata metadata2 = artifactMetadataService.getMetadata(STORAGE1, REPOSITORY_RELEASES,
                                                                             "org/carlspring/strongbox/metadata/strongbox-metadata");

                    assertNotNull(metadata1);
                    assertNotNull(metadata2);

                    Versioning versioning1 = metadata1.getVersioning();
                    Versioning versioning2 = metadata1.getVersioning();

                    assertEquals("Incorrect artifactId!", artifact1.getArtifactId(), metadata1.getArtifactId());
                    assertEquals("Incorrect groupId!", artifact1.getGroupId(), metadata1.getGroupId());

                    assertEquals("Incorrect artifactId!", artifact4.getArtifactId(), metadata2.getArtifactId());
                    assertEquals("Incorrect groupId!", artifact4.getGroupId(), metadata2.getGroupId());

                    assertNotNull("No versioning information could be found in the metadata!", versioning1.getVersions()
                                                                                                          .size());
                    assertEquals("Incorrect number of versions stored in metadata!", 1, versioning1.getVersions()
                                                                                                   .size());

                    assertNotNull("No versioning information could be found in the metadata!", versioning2.getVersions()
                                                                                                          .size());
                    assertEquals("Incorrect number of versions stored in metadata!", 1, versioning2.getVersions()
                                                                                                   .size());
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, RebuildMavenMetadataCronJob.class, null, null);

        assertTrue("Failed to execute task!", expectEvent());
    }
}
