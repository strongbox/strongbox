package org.carlspring.strongbox.testing.repository;

import org.carlspring.strongbox.config.PypiLayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.PypiLayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author ankit.tomar
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@Execution(CONCURRENT)
@ContextConfiguration(classes = PypiLayoutProviderTestConfig.class)
public class PypiRepositoryManagementTest
{
    private static final String PYPI_STORAGE = "prmt-storage";

    private static final String PYPI_REPOSITORY = "prmt-releases";

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testValidRepositoryShouldBeInjected(@PypiTestRepository(storageId = PYPI_STORAGE,
                                                                        repositoryId = PYPI_REPOSITORY) 
                                                    Repository repository)
    {
        assertThat(repository).isNotNull();
        assertThat(repository.getId()).isNotNull().isEqualTo(PYPI_REPOSITORY);
        assertThat(repository.getBasedir()).isNotNull();
        assertThat(repository.getStorage()).isNotNull();
        assertThat(repository.getStorage().getId()).isNotNull().isEqualTo(PYPI_STORAGE);
        assertThat(repository.getStorage().getBasedir()).isNotNull();
        assertThat(repository.getLayout()).isNotNull().isEqualTo(PypiLayoutProvider.ALIAS);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testValidRepositoryShouldBeInjectedWithDefaultStorage(@PypiTestRepository(repositoryId = PYPI_REPOSITORY) Repository repository)
    {
        assertThat(repository).isNotNull();
        assertThat(repository.getId()).isNotNull().isEqualTo(PYPI_REPOSITORY);
        assertThat(repository.getBasedir()).isNotNull();
        assertThat(repository.getStorage()).isNotNull();
        assertThat(repository.getStorage().getId()).isNotNull().isEqualTo("storage0");
        assertThat(repository.getStorage().getBasedir()).isNull(); // it will be null as storage baseDir is not present in strongbox.yaml
        assertThat(repository.getLayout()).isNotNull().isEqualTo(PypiLayoutProvider.ALIAS);
    }
}
