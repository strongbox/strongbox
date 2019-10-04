package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(SAME_THREAD)
public class Maven2LayoutProviderTest
{

    private static final String DELETE_FOO_1_2_1 = "com/artifacts/to/delete/releases/delete-foo/1.2.1/delete-foo-1.2.1.jar";
    
    private static final String DELETE_FOO_1_2_2 = "com/artifacts/to/delete/releases/delete-foo/1.2.2/delete-foo-1.2.2.jar";

    private static final String REPOSITORY_RELEASES = "m2lp-releases";

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testDeleteArtifact(@MavenRepository(repositoryId = REPOSITORY_RELEASES)
                                   Repository repository,
                                   @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                      resource = DELETE_FOO_1_2_1)
                                   Path artifactPath)
            throws IOException
    {
        RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();

        assertThat(Files.exists(artifactRepositoryPath))
                .as("Failed to locate artifact file " + artifactPath)
                .isTrue();

        RepositoryFiles.delete(artifactRepositoryPath, false);

        assertThat(Files.exists(artifactRepositoryPath))
                .as("Failed to delete artifact file " + artifactRepositoryPath)
                .isFalse();
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testDeleteArtifactDirectory(@MavenRepository(repositoryId = REPOSITORY_RELEASES)
                                            Repository repository,
                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                               resource = DELETE_FOO_1_2_2)
                                            Path artifactPath)
            throws IOException
    {
        RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();

        assertThat(Files.exists(artifactRepositoryPath))
                .as("Failed to locate artifact file " + artifactPath)
                .isTrue();

        RepositoryFiles.delete(artifactRepositoryPath, false);

        assertThat(Files.exists(artifactRepositoryPath))
                .as("Failed to delete artifact file " + artifactPath)
                .isFalse();
    }

}
