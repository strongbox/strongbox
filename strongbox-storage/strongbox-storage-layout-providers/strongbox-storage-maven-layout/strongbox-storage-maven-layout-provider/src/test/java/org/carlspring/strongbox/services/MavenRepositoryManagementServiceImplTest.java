package org.carlspring.strongbox.services;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
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
{

    private static final String REPOSITORY_RELEASES_1 = "rmsi-releases-1";

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testCreateRepository(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                      setup = MavenIndexedRepositorySetup.class)
                                     Repository repository)
    {
        Path repositoryPath = repositoryPathResolver.resolve(repository);
        assertThat(Files.exists(repositoryPath)).as("Failed to create repository '" + repository.getId() + "'!").isTrue();
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testCreateAndDelete(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                     setup = MavenIndexedRepositorySetup.class,
                                                     cleanup = false)
                                    Repository repository)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        Path repositoryPath = repositoryPathResolver.resolve(repository);
        assertThat(Files.exists(repositoryPath)).as("Failed to create repository '" + repositoryId + "'!").isTrue();

        repositoryManagementService.removeRepository(storageId, repositoryId);

        assertThat(Files.notExists(repositoryPath)).as("Failed to remove the repository!").isTrue();
    }

}
