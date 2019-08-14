package org.carlspring.strongbox.services;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenRepositoryManagementServiceImplTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES_1 = "rmsi-releases-1";

    private static final String REPOSITORY_RELEASES_MERGE_1 = "rmsi-releases-merge-1";

    private static final String REPOSITORY_RELEASES_MERGE_2 = "rmsi-releases-merge-2";

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testCreateRepository(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                      setup = MavenIndexedRepositorySetup.class)
                                     Repository repository)
    {
        Path repositoryPath = repositoryPathResolver.resolve(repository);
        assertTrue(Files.exists(repositoryPath), "Failed to create repository '" + repository.getId() + "'!");
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testCreateAndDelete(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                     setup = MavenIndexedRepositorySetup.class,
                                                     cleanup = false)
                                    Repository repository)
            throws Exception
    {
        Path repositoryPath = repositoryPathResolver.resolve(repository);
        assertTrue(Files.exists(repositoryPath), "Failed to create repository '" + repository.getId() + "'!");

        getRepositoryManagementService().removeRepository(STORAGE0, REPOSITORY_RELEASES_1);

        assertTrue(Files.notExists(repositoryPath), "Failed to remove the repository!");
    }

}
