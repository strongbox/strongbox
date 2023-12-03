package org.carlspring.strongbox.testing.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author ankit.tomar
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = NugetLayoutProviderTestConfig.class)
@Execution(ExecutionMode.CONCURRENT)
public class NugetRepositoryManagementTest
{

    private static final String NUGET_STORAGE = "nurmt-storage";

    private static final String NUGET_REPOSITORY = "nurmt-releases";

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testValidRepositoryShouldBeInjected(@NugetRepository(storageId = NUGET_STORAGE,
                                                                     repositoryId = NUGET_REPOSITORY)
                                                    Repository repository)
    {
        assertThat(repository).isNotNull();
        assertThat(repository.getId()).isNotNull().isEqualTo(NUGET_REPOSITORY);
        assertThat(repository.getBasedir()).isNotNull();
        assertThat(repository.getStorage()).isNotNull();
        assertThat(repository.getStorage().getId()).isNotNull().isEqualTo(NUGET_STORAGE);
        assertThat(repository.getStorage().getBasedir()).isNotNull();
        assertThat(repository.getLayout()).isNotNull().isEqualTo(NugetLayoutProvider.ALIAS);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testValidRepositoryShouldBeInjectedWithDefaultStorage(@NugetRepository(repositoryId = NUGET_REPOSITORY) Repository repository)
    {
        assertThat(repository).isNotNull();
        assertThat(repository.getId()).isNotNull().isEqualTo(NUGET_REPOSITORY);
        assertThat(repository.getBasedir()).isNotNull();
        assertThat(repository.getStorage()).isNotNull();
        assertThat(repository.getStorage().getId()).isNotNull().isEqualTo("storage-nuget");
        assertThat(repository.getStorage().getBasedir()).isNull();
        assertThat(repository.getLayout()).isNotNull().isEqualTo(NugetLayoutProvider.ALIAS);
    }
}
