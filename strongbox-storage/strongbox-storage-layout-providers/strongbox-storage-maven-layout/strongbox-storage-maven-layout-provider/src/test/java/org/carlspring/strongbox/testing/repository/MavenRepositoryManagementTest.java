package org.carlspring.strongbox.testing.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
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
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(ExecutionMode.CONCURRENT)
public class MavenRepositoryManagementTest
{

    private static final String MAVEN_STORAGE = "mrmt-storage";

    private static final String MAVEN_REPOSITORY = "mrmt-releases";

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testValidRepositoryShouldBeInjected(@MavenRepository(storageId = MAVEN_STORAGE,
                                                                     repositoryId = MAVEN_REPOSITORY)
                                                    Repository repository)
    {
        assertThat(repository).isNotNull();
        assertThat(repository.getId()).isNotNull().isEqualTo(MAVEN_REPOSITORY);
        assertThat(repository.getBasedir()).isNotNull();
        assertThat(repository.getStorage()).isNotNull();
        assertThat(repository.getStorage().getId()).isNotNull().isEqualTo(MAVEN_STORAGE);
        assertThat(repository.getStorage().getBasedir()).isNotNull();
        assertThat(repository.getLayout()).isNotNull().isEqualTo(Maven2LayoutProvider.ALIAS);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testValidRepositoryShouldBeInjectedWithDefaultStorage(@MavenRepository(repositoryId = MAVEN_REPOSITORY) Repository repository)
    {
        assertThat(repository).isNotNull();
        assertThat(repository.getId()).isNotNull().isEqualTo(MAVEN_REPOSITORY);
        assertThat(repository.getBasedir()).isNotNull();
        assertThat(repository.getStorage()).isNotNull();
        assertThat(repository.getStorage().getId()).isNotNull().isEqualTo("storage0");
        assertThat(repository.getStorage().getBasedir()).isNull();
        assertThat(repository.getLayout()).isNotNull().isEqualTo(Maven2LayoutProvider.ALIAS);
    }
}
