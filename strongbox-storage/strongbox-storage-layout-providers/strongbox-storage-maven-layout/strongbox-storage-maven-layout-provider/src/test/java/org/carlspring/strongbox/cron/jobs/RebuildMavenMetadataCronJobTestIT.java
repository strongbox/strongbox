package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.BeforeAll;
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
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
public class RebuildMavenMetadataCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String[] CLASSIFIERS = { "javadoc",
                                                  "sources",
                                                  "source-release" };

    @Inject
    private ArtifactMetadataService artifactMetadataService;


    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);
    }

    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, "rmmcjtit-snapshots", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "trmir-snapshots", Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    public void testRebuildArtifactsMetadata()
            throws Exception
    {
        String repositoryId = "rmmcjtit-snapshots";

        createRepository(STORAGE0,
                         repositoryId,
                         RepositoryPolicyEnum.SNAPSHOT.getPolicy(),
                         false);

        MavenArtifact artifact1 = createTestArtifact1(repositoryId);

        final String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                            repositoryId,
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

        addCronJobConfig(jobName,
                         RebuildMavenMetadataCronJob.class,
                         STORAGE0,
                         repositoryId,
                         properties -> properties.put("basePath", "org/carlspring/strongbox/strongbox-metadata-one"));

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());

        closeIndexersForRepository(STORAGE0, repositoryId);
    }

    @Test
    public void testRebuildMetadataInRepository()
            throws Exception
    {
        String repositoryId = "trmir-snapshots";

        createRepository(STORAGE0,
                         repositoryId,
                         RepositoryPolicyEnum.SNAPSHOT.getPolicy(),
                         false);

        MavenArtifact artifact1 = createTestArtifact1(repositoryId);
        MavenArtifact artifact2 = createTestArtifact2(repositoryId);

        createRepository(STORAGE0,
                         repositoryId,
                         RepositoryPolicyEnum.SNAPSHOT.getPolicy(),
                         false);

        final String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    Metadata metadata1 = artifactMetadataService.getMetadata(STORAGE0,
                                                                             repositoryId,
                                                                             "org/carlspring/strongbox/strongbox-metadata-one");
                    Metadata metadata2 = artifactMetadataService.getMetadata(STORAGE0,
                                                                             repositoryId,
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

        addCronJobConfig(jobName,
                         RebuildMavenMetadataCronJob.class,
                         STORAGE0,
                         repositoryId);

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());

        closeIndexersForRepository(STORAGE0, repositoryId);
    }


    private MavenArtifact createTestArtifact1(String repositoryId)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        return createTimestampedSnapshotArtifact(getRepositoryBasedir(STORAGE0, repositoryId).getAbsolutePath(),
                                                 "org.carlspring.strongbox",
                                                 "strongbox-metadata-one",
                                                 "2.0",
                                                 "jar",
                                                 CLASSIFIERS,
                                                 5);
    }

    private MavenArtifact createTestArtifact2(String repositoryId)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        return createTimestampedSnapshotArtifact(getRepositoryBasedir(STORAGE0, repositoryId).getAbsolutePath(),
                                                 "org.carlspring.strongbox",
                                                 "strongbox-metadata-second",
                                                 "2.0",
                                                 "jar",
                                                 CLASSIFIERS,
                                                 5);
    }

}
