package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
public class RebuildMavenMetadataCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String STORAGE1 = "storage1";

    private static final String REPOSITORY_RELEASES = "rmmcj-releases";

    private static final String REPOSITORY_SNAPSHOTS = "rmmcj-snapshots";

    private static final File REPOSITORY_RELEASES_BASEDIR_1 = new File(
            ConfigurationResourceResolver.getVaultDirectory() +
            "/storages/" + STORAGE0 + "/" +
            REPOSITORY_RELEASES);

    private static final File REPOSITORY_SNAPSHOTS_BASEDIR = new File(
            ConfigurationResourceResolver.getVaultDirectory() +
            "/storages/" + STORAGE0 + "/" +
            REPOSITORY_SNAPSHOTS);

    private static final File REPOSITORY_RELEASES_BASEDIR_2 = new File(
            ConfigurationResourceResolver.getVaultDirectory() +
            "/storages/" + STORAGE1 + "/" +
            REPOSITORY_RELEASES);

    private static final String[] CLASSIFIERS = { "javadoc",
                                                  "sources",
                                                  "source-release" };

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_METADATA = "org/carlspring/strongbox/strongbox-metadata-one";

    private static MavenArtifact artifact1;

    private static MavenArtifact artifact2;

    private static MavenArtifact artifact3;

    private static MavenArtifact artifact4;

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    private String getRepositoryName(String repositoryId,
                                     String methodName)
    {
        return repositoryId + methodName;
    }

    private Set<MutableRepository> getRepositories(TestInfo testInfo)
    {
        String methodName = testInfo.getTestMethod().get().getName();

        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, getRepositoryName(REPOSITORY_RELEASES, methodName),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, getRepositoryName(REPOSITORY_SNAPSHOTS, methodName),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE1, getRepositoryName(REPOSITORY_RELEASES, methodName),
                                              Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);
        String methodName = testInfo.getTestMethod().get().getName();

        createRepository(STORAGE0, getRepositoryName(REPOSITORY_SNAPSHOTS, methodName),
                         RepositoryPolicyEnum.SNAPSHOT.getPolicy(), false);

        artifact1 = createTimestampedSnapshotArtifact(getRepositoryBasedir(REPOSITORY_SNAPSHOTS_BASEDIR, methodName),
                                                      "org.carlspring.strongbox",
                                                      "strongbox-metadata-one",
                                                      "2.0",
                                                      "jar",
                                                      CLASSIFIERS,
                                                      5);

        artifact2 = createTimestampedSnapshotArtifact(getRepositoryBasedir(REPOSITORY_SNAPSHOTS_BASEDIR, methodName),
                                                      "org.carlspring.strongbox",
                                                      "strongbox-metadata-second",
                                                      "2.0",
                                                      "jar",
                                                      CLASSIFIERS,
                                                      5);

        createRepository(STORAGE0, getRepositoryName(REPOSITORY_RELEASES, methodName),
                         RepositoryPolicyEnum.RELEASE.getPolicy(), false);

        artifact3 = generateArtifact(getRepositoryBasedir(REPOSITORY_RELEASES_BASEDIR_1, methodName),
                                     "org.carlspring.strongbox.metadata:strongbox-metadata:1.0:jar");

        createStorage(STORAGE1);

        createRepository(STORAGE1, getRepositoryName(REPOSITORY_RELEASES, methodName),
                         RepositoryPolicyEnum.RELEASE.getPolicy(), false);

        artifact4 = generateArtifact(getRepositoryBasedir(REPOSITORY_RELEASES_BASEDIR_2, methodName),
                                     "org.carlspring.strongbox.metadata:strongbox-metadata:1.0:jar");
    }

    private String getRepositoryBasedir(File repositoryBaseDir,
                                        String methodName)
    {
        return repositoryBaseDir.getAbsolutePath() + methodName;
    }

    @AfterEach
    public void removeRepositories(TestInfo testInfo)
            throws IOException, JAXBException
    {
        String methodName = testInfo.getTestMethod().get().getName();
        closeIndexersForRepository(STORAGE0, getRepositoryName(REPOSITORY_RELEASES, methodName));
        closeIndexersForRepository(STORAGE0, getRepositoryName(REPOSITORY_SNAPSHOTS, methodName));
        closeIndexersForRepository(STORAGE1, getRepositoryName(REPOSITORY_RELEASES, methodName));
        removeRepositories(getRepositories(testInfo));
    }

    @Test
    public void testRebuildArtifactsMetadata(TestInfo testInfo)
            throws Exception
    {
        String methodName = testInfo.getTestMethod().get().getName();
        final String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                            getRepositoryName(REPOSITORY_SNAPSHOTS,
                                                                                              methodName),
                                                                            "org/carlspring/strongbox/strongbox-metadata-one");

                    assertNotNull(metadata);

                    Versioning versioning = metadata.getVersioning();

                    assertEquals(artifact1.getArtifactId(), metadata.getArtifactId(), "Incorrect artifactId!");
                    assertEquals(artifact1.getGroupId(), metadata.getGroupId(), "Incorrect groupId!");

                    assertNotNull(versioning.getVersions(),
                                  "No versioning information could be found in the metadata!");
                    assertEquals(1, versioning.getVersions().size(),
                                 "Incorrect number of versions stored in metadata!");
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobName, RebuildMavenMetadataCronJob.class, STORAGE0,
                         getRepositoryName(REPOSITORY_SNAPSHOTS, methodName),
                         properties -> properties.put("basePath", ARTIFACT_BASE_PATH_STRONGBOX_METADATA));

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @Test
    public void testRebuildMetadataInRepository(TestInfo testInfo)
            throws Exception
    {
        String methodName = testInfo.getTestMethod().get().getName();
        final String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    Metadata metadata1 = artifactMetadataService.getMetadata(STORAGE0,
                                                                             getRepositoryName(REPOSITORY_SNAPSHOTS,
                                                                                               methodName),
                                                                             "org/carlspring/strongbox/strongbox-metadata-one");
                    Metadata metadata2 = artifactMetadataService.getMetadata(STORAGE0,
                                                                             getRepositoryName(REPOSITORY_SNAPSHOTS,
                                                                                               methodName),
                                                                             "org/carlspring/strongbox/strongbox-metadata-second");

                    assertNotNull(metadata1);
                    assertNotNull(metadata2);

                    Versioning versioning1 = metadata1.getVersioning();
                    Versioning versioning2 = metadata1.getVersioning();

                    assertEquals(artifact1.getArtifactId(), metadata1.getArtifactId(), "Incorrect artifactId!");
                    assertEquals(artifact1.getGroupId(), metadata1.getGroupId(), "Incorrect groupId!");

                    assertEquals(artifact2.getArtifactId(), metadata2.getArtifactId(), "Incorrect artifactId!");
                    assertEquals(artifact2.getGroupId(), metadata2.getGroupId(), "Incorrect groupId!");

                    assertNotNull(versioning1.getVersions(),
                                  "No versioning information could be found in the metadata!");
                    assertEquals(1, versioning1.getVersions().size(),
                                 "Incorrect number of versions stored in metadata!");

                    assertNotNull(versioning2.getVersions(),
                                  "No versioning information could be found in the metadata!");
                    assertEquals(1, versioning2.getVersions().size(),
                                 "Incorrect number of versions stored in metadata!");
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobName, RebuildMavenMetadataCronJob.class, STORAGE0,
                         getRepositoryName(REPOSITORY_SNAPSHOTS, methodName));

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @Test
    public void testRebuildMetadataInStorage(TestInfo testInfo)
            throws Exception
    {
        String methodName = testInfo.getTestMethod().get().getName();
        final String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    Metadata metadata1 = artifactMetadataService.getMetadata(STORAGE0,
                                                                             getRepositoryName(REPOSITORY_SNAPSHOTS,
                                                                                               methodName),
                                                                             "org/carlspring/strongbox/strongbox-metadata-one");
                    Metadata metadata2 = artifactMetadataService.getMetadata(STORAGE0,
                                                                             getRepositoryName(REPOSITORY_RELEASES,
                                                                                               methodName),
                                                                             "org/carlspring/strongbox/metadata/strongbox-metadata");

                    assertNotNull(metadata1);
                    assertNotNull(metadata2);

                    Versioning versioning1 = metadata1.getVersioning();
                    Versioning versioning2 = metadata1.getVersioning();

                    assertEquals(artifact1.getArtifactId(), metadata1.getArtifactId(), "Incorrect artifactId!");
                    assertEquals(artifact1.getGroupId(), metadata1.getGroupId(), "Incorrect groupId!");

                    assertEquals(artifact3.getArtifactId(), metadata2.getArtifactId(), "Incorrect artifactId!");
                    assertEquals(artifact3.getGroupId(), metadata2.getGroupId(), "Incorrect groupId!");

                    assertNotNull(versioning1.getVersions(),
                                  "No versioning information could be found in the metadata!");
                    assertEquals(1, versioning1.getVersions().size(),
                                 "Incorrect number of versions stored in metadata!");

                    assertNotNull(versioning2.getVersions(),
                                  "No versioning information could be found in the metadata!");
                    assertEquals(1, versioning2.getVersions().size(),
                                 "Incorrect number of versions stored in metadata!");
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobName, RebuildMavenMetadataCronJob.class, STORAGE0, null);

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @Test
    public void testRebuildMetadataInStorages(TestInfo testInfo)
            throws Exception
    {
        String methodName = testInfo.getTestMethod().get().getName();
        final String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    Metadata metadata1 = artifactMetadataService.getMetadata(STORAGE0,
                                                                             getRepositoryName(REPOSITORY_SNAPSHOTS,
                                                                                               methodName),
                                                                             "org/carlspring/strongbox/strongbox-metadata-one");
                    Metadata metadata2 = artifactMetadataService.getMetadata(STORAGE1,
                                                                             getRepositoryName(REPOSITORY_RELEASES,
                                                                                               methodName),
                                                                             "org/carlspring/strongbox/metadata/strongbox-metadata");

                    assertNotNull(metadata1);
                    assertNotNull(metadata2);

                    Versioning versioning1 = metadata1.getVersioning();
                    Versioning versioning2 = metadata1.getVersioning();

                    assertEquals(artifact1.getArtifactId(), metadata1.getArtifactId(), "Incorrect artifactId!");
                    assertEquals(artifact1.getGroupId(), metadata1.getGroupId(), "Incorrect groupId!");

                    assertEquals(artifact4.getArtifactId(), metadata2.getArtifactId(), "Incorrect artifactId!");
                    assertEquals(artifact4.getGroupId(), metadata2.getGroupId(), "Incorrect groupId!");

                    assertNotNull(versioning1.getVersions(),
                                  "No versioning information could be found in the metadata!");
                    assertEquals(1, versioning1.getVersions().size(),
                                 "Incorrect number of versions stored in metadata!");

                    assertNotNull(versioning2.getVersions(),
                                  "No versioning information could be found in the metadata!");
                    assertEquals(1, versioning2.getVersions().size(),
                                 "Incorrect number of versions stored in metadata!");
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobName, RebuildMavenMetadataCronJob.class, null, null);

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }
}
