package org.carlspring.strongbox.cron.jobs;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryAttributes;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

/**
 * @author Kate Novik.
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

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    public void testRemoveTrashInRepository(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1)
                                            @RepositoryAttributes(allowsForceDeletion = true, trashEnabled = true)
                                            Repository repository1,
                                            @MavenRepository(repositoryId = REPOSITORY_RELEASES_2)
                                            @RepositoryAttributes(allowsForceDeletion = false, trashEnabled = true)
                                            Repository repository2,
                                            @MavenRepository(storageId = STORAGE1, repositoryId = REPOSITORY_RELEASES_1)
                                            @RepositoryAttributes(allowsForceDeletion = false, trashEnabled = true)
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
        RepositoryPath repositoryRootPath = repositoryPathResolver.resolve(repository1);
        RepositoryPath repositoryTrashPath = RepositoryFiles.trash(repositoryRootPath);
        RepositoryPath path = repositoryPathResolver.resolve(repository1,
                                                             "org/carlspring/strongbox/clear/strongbox-test-one/1.0");
        RepositoryPath trashPath = RepositoryFiles.trash(path);
        
        assertTrue(Files.exists(repositoryTrashPath), "There is no path to the repository trash!");
        assertFalse(Files.exists(trashPath), "The repository trash isn't empty!");

        RepositoryFiles.delete(path, false);

        assertNotNull(Files.exists(repositoryTrashPath), "There is no path to the repository trash!");
        assertFalse(Files.exists(artifact1.normalize()), "The repository path exists!");
        assertTrue(Files.exists(RepositoryFiles.trash((RepositoryPath) artifact1.normalize())), "The repository trash is empty!");

        addCronJobConfig(expectedJobName, ClearRepositoryTrashCronJob.class, STORAGE0, REPOSITORY_RELEASES_1);

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
        
        assertTrue(Files.exists(repositoryTrashPath), "There is no path to the repository trash!");
        assertFalse(Files.exists(RepositoryFiles.trash((RepositoryPath) artifact1.normalize())), "The repository trash isn't empty!");        
    }

}
