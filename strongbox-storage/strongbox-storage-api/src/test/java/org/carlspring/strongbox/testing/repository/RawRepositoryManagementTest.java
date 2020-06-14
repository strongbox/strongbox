package org.carlspring.strongbox.testing.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.NullLayoutProvider;
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
@ContextConfiguration(classes = StorageApiTestConfig.class)
@Execution(ExecutionMode.CONCURRENT)
public class RawRepositoryManagementTest
{

    private static final String RAW_STORAGE = "raw-storage-to-be-injected";

    private static final String RAW_REPOSITORY = "rwa-repository-to-be-injected";

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testValidRepositoryShouldBeInjected(@NullRepository(repositoryId = RAW_REPOSITORY,
                                                                    storageId = RAW_STORAGE)
                                                    Repository repository)
    {
        assertThat(repository).isNotNull();
        assertThat(repository.getId()).isNotNull().isEqualTo(RAW_REPOSITORY);
        assertThat(repository.getBasedir()).isNotNull();
        assertThat(repository.getStorage()).isNotNull();
        assertThat(repository.getStorage().getId()).isNotNull().isEqualTo(RAW_STORAGE);
        assertThat(repository.getStorage().getBasedir()).isNotNull();
        assertThat(repository.getLayout()).isNotNull().isEqualTo(NullLayoutProvider.ALIAS);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testValidRepositoryShouldBeInjectedWithDefaultStorage(@NullRepository(repositoryId = RAW_REPOSITORY) Repository repository)
    {
        assertThat(repository).isNotNull();
        assertThat(repository.getId()).isNotNull().isEqualTo(RAW_REPOSITORY);
        assertThat(repository.getBasedir()).isNotNull();
        assertThat(repository.getStorage()).isNotNull();
        assertThat(repository.getStorage().getId()).isNotNull().isEqualTo("storage0");
        assertThat(repository.getStorage().getBasedir()).isNull();
        assertThat(repository.getLayout()).isNotNull().isEqualTo(NullLayoutProvider.ALIAS);
    }
}
