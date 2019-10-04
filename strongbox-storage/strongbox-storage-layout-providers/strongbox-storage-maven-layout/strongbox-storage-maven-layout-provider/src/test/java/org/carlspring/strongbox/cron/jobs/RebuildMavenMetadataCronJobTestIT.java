package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
public class RebuildMavenMetadataCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String RMMCJTIT_SNAPSHOTS = "rmmcjtit-snapshots";

    private static final String TRMIR_SNAPSHOTS = "trmir-snapshots";

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRebuildArtifactsMetadata(@MavenRepository(repositoryId = RMMCJTIT_SNAPSHOTS,
                                                              policy = RepositoryPolicyEnum.SNAPSHOT)
                                             Repository repository,
                                             @MavenTestArtifact(repositoryId = RMMCJTIT_SNAPSHOTS,
                                                                id = "org.carlspring.strongbox:strongbox-metadata-one",
                                                                versions = { "2.0-20190512.202015-1",
                                                                             "2.0-20190512.202015-2",
                                                                             "2.0-20190512.202015-3",
                                                                             "2.0-20190512.202015-4",
                                                                             "2.0-20190512.202015-5" },
                                                                classifiers = { "javadoc",
                                                                                "sources",
                                                                                "source-release" })
                                             List<Path> artifact)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        String artifactId = "strongbox-metadata-one";

        String groupId = "org.carlspring.strongbox";

        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
            {
                try
                {
                    Metadata metadata = artifactMetadataService.getMetadata(storageId,
                                                                            repositoryId,
                                                                            "org/carlspring/strongbox/strongbox-metadata-one");

                    assertThat(metadata).isNotNull();

                    Versioning versioning = metadata.getVersioning();

                    assertThat(metadata.getArtifactId()).as("Incorrect artifactId!").isEqualTo(artifactId);
                    assertThat(metadata.getGroupId()).as("Incorrect groupId!").isEqualTo(groupId);

                    assertThat(versioning.getVersions())
                            .as("No versioning information could be found in the metadata!").isNotNull();
                    assertThat(versioning.getVersions())
                            .as("Incorrect number of versions stored in metadata!").hasSize(1);
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobKey,
                         jobName,
                         RebuildMavenMetadataCronJob.class,
                         storageId,
                         repositoryId,
                         properties -> properties.put("basePath", "org/carlspring/strongbox/strongbox-metadata-one"));

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRebuildMetadataInRepository(@MavenRepository(repositoryId = TRMIR_SNAPSHOTS,
                                                                 policy = RepositoryPolicyEnum.SNAPSHOT)
                                                Repository repository,
                                                @MavenTestArtifact(repositoryId = TRMIR_SNAPSHOTS,
                                                                   id = "org.carlspring.strongbox:strongbox-metadata-one",
                                                                   versions = { "2.0-20190512.202015-1",
                                                                                "2.0-20190512.202015-2",
                                                                                "2.0-20190512.202015-3",
                                                                                "2.0-20190512.202015-4",
                                                                                "2.0-20190512.202015-5" },
                                                                   classifiers = { "javadoc",
                                                                                   "sources",
                                                                                   "source-release" })
                                                List<Path> artifact1,
                                                @MavenTestArtifact(repositoryId = TRMIR_SNAPSHOTS,
                                                                   id = "org.carlspring.strongbox:strongbox-metadata-second",
                                                                   versions = { "2.0-20190512.202015-1",
                                                                                "2.0-20190512.202015-2",
                                                                                "2.0-20190512.202015-3",
                                                                                "2.0-20190512.202015-4",
                                                                                "2.0-20190512.202015-5" },
                                                                   classifiers = { "javadoc",
                                                                                   "sources",
                                                                                   "source-release" })
                                                List<Path> artifact2)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String artifactId1 = "strongbox-metadata-one";

        String artifactId2 = "strongbox-metadata-second";

        String groupId = "org.carlspring.strongbox";

        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
            {
                try
                {
                    Metadata metadata1 = artifactMetadataService.getMetadata(storageId,
                                                                             repositoryId,
                                                                             "org/carlspring/strongbox/strongbox-metadata-one");
                    Metadata metadata2 = artifactMetadataService.getMetadata(storageId,
                                                                             repositoryId,
                                                                             "org/carlspring/strongbox/strongbox-metadata-second");

                    assertThat(metadata1).isNotNull();
                    assertThat(metadata2).isNotNull();

                    Versioning versioning1 = metadata1.getVersioning();
                    Versioning versioning2 = metadata1.getVersioning();

                    assertThat(metadata1.getArtifactId()).as( "Incorrect artifactId!").isEqualTo(artifactId1);
                    assertThat(metadata1.getGroupId()).as("Incorrect groupId!").isEqualTo(groupId);

                    assertThat(metadata2.getArtifactId()).as( "Incorrect artifactId!").isEqualTo(artifactId2);
                    assertThat(metadata2.getGroupId()).as("Incorrect groupId!").isEqualTo(groupId);

                    assertThat(versioning1.getVersions())
                            .as("No versioning information could be found in the metadata!").isNotNull();
                    assertThat(versioning1.getVersions())
                            .as("Incorrect number of versions stored in metadata!").hasSize(1);

                    assertThat(versioning2.getVersions()).as("No versioning information could be found in the metadata!").isNotNull();
                    assertThat(versioning2.getVersions()).as("Incorrect number of versions stored in metadata!").hasSize(1);
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobKey,
                         jobName,
                         RebuildMavenMetadataCronJob.class,
                         storageId,
                         repositoryId);

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }
}
