package org.carlspring.strongbox.cron.jobs;

import static org.carlspring.strongbox.util.TestFileUtils.deleteIfExists;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class RegenerateMavenChecksumCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String STORAGE1 = "storage1";

    private static final String REPOSITORY_RELEASES = "rmccj-releases";

    private static final String REPOSITORY_SNAPSHOTS = "rmccj-snapshots";

    private static final File REPOSITORY_RELEASES_BASEDIR_1 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE0 + "/" +
                                                                       REPOSITORY_RELEASES);

    private static final File REPOSITORY_SNAPSHOTS_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                      "/storages/" + STORAGE0 + "/" +
                                                                      REPOSITORY_SNAPSHOTS);

    private static final File REPOSITORY_RELEASES_BASEDIR_2 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE1 + "/" +
                                                                       REPOSITORY_RELEASES);

    private static MavenArtifact snapshotArtifact_1;

    private static MavenArtifact snapshotArtifact_2;

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
    private ArtifactMetadataService artifactMetadataService;

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_SNAPSHOTS, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_RELEASES, Maven2LayoutProvider.ALIAS));
        return repositories;
    }

    @Before
    public void initialize()
            throws Exception
    {
        createRepository(STORAGE0, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy(), false);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.toString(),
                         "org.carlspring.strongbox.checksum:strongbox-checksum:1.0:jar");

        createRepository(STORAGE0, REPOSITORY_SNAPSHOTS, RepositoryPolicyEnum.SNAPSHOT.getPolicy(), false);

        snapshotArtifact_1 = createTimestampedSnapshotArtifact(REPOSITORY_SNAPSHOTS_BASEDIR.toString(),
                                                               "org.carlspring.strongbox",
                                                               "strongbox-checksum-one",
                                                               "2.0",
                                                               "jar",
                                                               null,
                                                               1);
        snapshotArtifact_2 = createTimestampedSnapshotArtifact(REPOSITORY_SNAPSHOTS_BASEDIR.toString(),
                                                               "org.carlspring.strongbox",
                                                               "strongbox-checksum-second",
                                                               "2.0",
                                                               "jar",
                                                               null,
                                                               1);

        createStorage(STORAGE1);

        createRepository(STORAGE1, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy(), false);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_2.toString(),
                         "org.carlspring.strongbox.checksum:strongbox-checksum:1.0:jar");
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES);
        closeIndexersForRepository(STORAGE0, REPOSITORY_SNAPSHOTS);
        closeIndexersForRepository(STORAGE1, REPOSITORY_RELEASES);
        removeRepositories(getRepositoriesToClean());
    }

    @Test
    public void testRegenerateArtifactChecksum()
            throws Exception
    {
        String jobName = expectedJobName;

        String artifactPath = REPOSITORY_SNAPSHOTS_BASEDIR + "/org/carlspring/strongbox/strongbox-checksum-one";

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS,
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
                    assertTrue("The checksum file for artifact doesn't exist!",
                               new File(snapshotArtifact_1.getPath().toString() + ".sha1").exists());
                    assertTrue("The checksum file for artifact is empty!",
                               new File(snapshotArtifact_1.getPath().toString() + ".sha1").length() > 0);

                    assertTrue("The checksum file for artifact doesn't exist!",
                               new File(snapshotArtifact_1.getPath().toString() + ".md5").exists());
                    assertTrue("The checksum file for artifact is empty!",
                               new File(snapshotArtifact_1.getPath().toString() + ".md5").length() > 0);

                    assertTrue("The checksum file for pom file doesn't exist!",
                               new File(snapshotArtifact_1.getPath().toString().replaceAll("jar", "pom") +
                                        ".sha1").exists());
                    assertTrue("The checksum file for pom file is empty!",
                               new File(snapshotArtifact_1.getPath().toString().replaceAll("jar", "pom") +
                                        ".md5").length() > 0);

                    assertTrue("The checksum file for metadata file doesn't exist!",
                               new File(artifactPath, "/maven-metadata.xml.md5").exists());
                    assertTrue("The checksum file for metadata file is empty!",
                               new File(artifactPath, "/maven-metadata.xml.sha1").length() > 0);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, RegenerateChecksumCronJob.class, STORAGE0, REPOSITORY_SNAPSHOTS,
                         properties ->
                         {
                             properties.put("basePath", "org/carlspring/strongbox/strongbox-checksum-one");
                             properties.put("forceRegeneration", "false");
                         });

        assertTrue("Failed to execute task!", expectEvent());
    }

    @Test
    public void testRegenerateChecksumInRepository()
            throws Exception
    {
        String jobName = expectedJobName;

        String artifactPath = REPOSITORY_SNAPSHOTS_BASEDIR + "/org/carlspring/strongbox/strongbox-checksum-second";

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS,
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
                    assertTrue("The checksum file for artifact doesn't exist!",
                               new File(snapshotArtifact_2.getPath().toString() + ".sha1").exists());
                    assertTrue("The checksum file for artifact is empty!",
                               new File(snapshotArtifact_2.getPath().toString() + ".sha1").length() > 0);

                    assertTrue("The checksum file for artifact doesn't exist!",
                               new File(snapshotArtifact_2.getPath().toString() + ".md5").exists());
                    assertTrue("The checksum file for artifact is empty!",
                               new File(snapshotArtifact_2.getPath().toString() + ".md5").length() > 0);

                    assertTrue("The checksum file for pom file doesn't exist!",
                               new File(snapshotArtifact_2.getPath().toString().replaceAll("jar", "pom") +
                                        ".sha1").exists());
                    assertTrue("The checksum file for pom file is empty!",
                               new File(snapshotArtifact_2.getPath().toString().replaceAll("jar", "pom") +
                                        ".sha1").length() > 0);

                    assertTrue("The checksum file for metadata file doesn't exist!",
                               new File(artifactPath, "/maven-metadata.xml.md5").exists());
                    assertTrue("The checksum file for metadata file is empty!",
                               new File(artifactPath, "/maven-metadata.xml.sha1").length() > 0);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, RegenerateChecksumCronJob.class, STORAGE0, REPOSITORY_SNAPSHOTS,
                         properties -> properties.put("forceRegeneration", "false"));

        assertTrue("Failed to execute task!", expectEvent());
    }

    @Test
    public void testRegenerateChecksumInStorage()
            throws Exception
    {
        String jobName = expectedJobName;

        String artifactPath = REPOSITORY_RELEASES_BASEDIR_1 + "/org/carlspring/strongbox/checksum/strongbox-checksum";

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_RELEASES,
                                                "org/carlspring/strongbox/checksum/strongbox-checksum");

        deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5"));
        deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.sha1"));
        deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.md5"));
        deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.sha1"));

        deleteIfExists(new File(artifactPath, "/maven-metadata.xml.md5"));
        deleteIfExists(new File(artifactPath, "/maven-metadata.xml.sha1"));

        assertTrue("The checksum file for artifact exist!", !new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5").exists());

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {

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
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, RegenerateChecksumCronJob.class, STORAGE0, null,
                         properties -> properties.put("forceRegeneration", "false"));

        assertTrue("Failed to execute task!", expectEvent());
    }

    @Test
    public void testRegenerateChecksumInStorages()
            throws Exception
    {
        String jobName = expectedJobName;

        String artifactPath = REPOSITORY_RELEASES_BASEDIR_2 + "/org/carlspring/strongbox/checksum/strongbox-checksum";

        artifactMetadataService.rebuildMetadata(STORAGE1, REPOSITORY_RELEASES,
                                                "org/carlspring/strongbox/checksum/strongbox-checksum");

        deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5"));
        deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.sha1"));
        deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.md5"));
        deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.sha1"));

        deleteIfExists(new File(artifactPath, "/maven-metadata.xml.md5"));
        deleteIfExists(new File(artifactPath, "/maven-metadata.xml.sha1"));

        assertTrue("The checksum file for artifact exist!", !new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5").exists());

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
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
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, RegenerateChecksumCronJob.class, null, null,
                         properties -> properties.put("forceRegeneration", "false"));

        assertTrue("Failed to execute task!", expectEvent());
    }

}
