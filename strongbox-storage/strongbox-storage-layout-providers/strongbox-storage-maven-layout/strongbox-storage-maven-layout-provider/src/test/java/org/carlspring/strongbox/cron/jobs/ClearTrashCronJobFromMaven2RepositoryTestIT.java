package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryAttributes;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
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
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
public class ClearTrashCronJobFromMaven2RepositoryTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String STORAGE1 = "storage1";

    private static final String REPOSITORY_RELEASES_1 = "crtcj-releases";

    private static final String REPOSITORY_RELEASES_2 = "crtcj-releases-test";

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Disabled("See https://github.com/strongbox/strongbox/issues/1973")
    public void testRemoveTrashInRepository(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1)
                                            @RepositoryAttributes(allowsForceDeletion = true, trashEnabled = true)
                                            Repository repository1,
                                            @MavenRepository(repositoryId = REPOSITORY_RELEASES_2)
                                            @RepositoryAttributes(trashEnabled = true)
                                            Repository repository2,
                                            @MavenRepository(storageId = STORAGE1, repositoryId = REPOSITORY_RELEASES_1)
                                            @RepositoryAttributes(trashEnabled = true)
                                            Repository repository3,
                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                               id = "org.carlspring.strongbox.clear:strongbox-test-one",
                                                               versions = "1.0")
                                            Path artifact1,
                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_2,
                                                               id = "org.carlspring.strongbox.clear:strongbox-test-two",
                                                               versions = "1.0")
                                            Path artifact2,
                                            @MavenTestArtifact(storageId = STORAGE1,
                                                               repositoryId = REPOSITORY_RELEASES_1,
                                                               id = "org.carlspring.strongbox.clear:strongbox-test-one",
                                                               versions = "1.0")
                                            Path artifact3)
            throws Exception
    {
        final String storage1Id = repository1.getStorage().getId();
        final String repository1Id = repository1.getId();

        RepositoryPath repositoryRootPath = repositoryPathResolver.resolve(repository1);
        RepositoryPath repositoryTrashPath = RepositoryFiles.trash(repositoryRootPath);
        RepositoryPath path = repositoryPathResolver.resolve(repository1, (RepositoryPath) artifact1.normalize());
        RepositoryPath trashPath = RepositoryFiles.trash(path);

        assertThat(Files.exists(repositoryTrashPath))
                .as("There is no path to the repository trash!")
                .isTrue();
        assertThat(Files.exists(trashPath))
                .as("The repository trash isn't empty!")
                .isFalse();

        RepositoryFiles.delete(path, false);

        assertThat(Files.exists(repositoryTrashPath)).as("There is no path to the repository trash!").isTrue();
        assertThat(Files.exists(artifact1.normalize())).as("The repository path exists!").isFalse();
        assertThat(Files.exists(RepositoryFiles.trash((RepositoryPath) artifact1.normalize())))
                .as("The repository trash is empty!")
                .isTrue();

        addCronJobConfig(expectedJobKey,
                         expectedJobName,
                         ClearRepositoryTrashCronJob.class,
                         storage1Id,
                         repository1Id);

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());

        assertThat(Files.exists(repositoryTrashPath)).as("There is no path to the repository trash!").isTrue();
        assertThat(Files.exists(RepositoryFiles.trash((RepositoryPath) artifact1.normalize())))
                .as("The repository trash isn't empty!")
                .isFalse();
    }

}
