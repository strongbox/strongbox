package org.carlspring.strongbox.testing.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.carlspring.strongbox.config.NpmLayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
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
@ContextConfiguration(classes = NpmLayoutProviderTestConfig.class)
@Execution(ExecutionMode.CONCURRENT)
public class NpmRepositoryManagementTest
{

    private static final String NPM_STORAGE = "nrmt-storage";

    private static final String NPM_REPOSITORY = "nrmt-releases";

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testValidRepositoryShouldBeInjected(@NpmRepository(storageId = NPM_STORAGE,
                                                                   repositoryId = NPM_REPOSITORY)
                                                    Repository repository)
    {
        assertThat(repository).isNotNull();
        assertThat(repository.getId()).isNotNull().isEqualTo(NPM_REPOSITORY);
        assertThat(repository.getBasedir()).isNotNull();
        assertThat(repository.getStorage()).isNotNull();
        assertThat(repository.getStorage().getId()).isNotNull().isEqualTo(NPM_STORAGE);
        assertThat(repository.getStorage().getBasedir()).isNotNull();
        assertThat(repository.getLayout()).isNotNull().isEqualTo(NpmLayoutProvider.ALIAS);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testValidRepositoryShouldBeInjectedWithDefaultStorage(@NpmRepository(repositoryId = NPM_REPOSITORY) Repository repository)
    {
        assertThat(repository).isNotNull();
        assertThat(repository.getId()).isNotNull().isEqualTo(NPM_REPOSITORY);
        assertThat(repository.getBasedir()).isNotNull();
        assertThat(repository.getStorage()).isNotNull();
        assertThat(repository.getStorage().getId()).isNotNull().isEqualTo("storage-npm");
        assertThat(repository.getStorage().getBasedir()).isNull();
        assertThat(repository.getLayout()).isNotNull().isEqualTo(NpmLayoutProvider.ALIAS);
    }
}
