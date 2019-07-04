package org.carlspring.strongbox.providers.layout;

import static org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates.LAYOUT_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author mtodorov
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(SAME_THREAD)
public class Maven2LayoutProviderTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String DELETE_FOO_1_2_1 = "com/artifacts/to/delete/releases/delete-foo/1.2.1/delete-foo-1.2.1.jar";
    
    private static final String DELETE_FOO_1_2_2 = "com/artifacts/to/delete/releases/delete-foo/1.2.2/delete-foo-1.2.2.jar";

    private static final String REPOSITORY_RELEASES = "m2lp-releases";

    @Test
    @ExtendWith({RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testDeleteArtifact(@TestRepository(layout = LAYOUT_NAME, repositoryId = REPOSITORY_RELEASES) Repository repository,
                                   @TestArtifact(repositoryId = REPOSITORY_RELEASES, resource = DELETE_FOO_1_2_1, generator = MavenArtifactGenerator.class) Path artifactPath)
            throws IOException
    {
        assertTrue(Files.exists(artifactPath), "Failed to locate artifact file " + artifactPath);

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);
        repositoryPath = repositoryPath.resolve(repositoryPath.relativize(artifactPath));
        RepositoryFiles.delete(repositoryPath, false);           

        assertFalse(Files.exists(artifactPath), "Failed to delete artifact file " + artifactPath);
    }

    @Test
    @ExtendWith({RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testDeleteArtifactDirectory(@TestRepository(layout = LAYOUT_NAME, repositoryId = REPOSITORY_RELEASES) Repository repository,
                                            @TestArtifact(repositoryId = REPOSITORY_RELEASES, resource = DELETE_FOO_1_2_2, generator = MavenArtifactGenerator.class) Path artifactPath)
            throws IOException
    {

        assertTrue(Files.exists(artifactPath), "Failed to locate artifact file " + artifactPath);

        RepositoryPath repositoryPath = (RepositoryPath) artifactPath.getParent();
        RepositoryFiles.delete(repositoryPath, false);

        assertFalse(Files.exists(artifactPath), "Failed to delete artifact file " + artifactPath);
    }

}
