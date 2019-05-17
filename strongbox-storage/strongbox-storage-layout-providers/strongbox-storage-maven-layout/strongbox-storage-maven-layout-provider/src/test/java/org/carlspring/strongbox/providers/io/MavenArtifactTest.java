package org.carlspring.strongbox.providers.io;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenArtifactTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "mrpl-releases";

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void artifactWithUnderscoreShouldWork(@MavenRepository(repositoryId = REPOSITORY_RELEASES) Repository repository,
                                                 @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES, id = "org.bitbucket.b_c:jose4j", versions = "0.6.3") Path path)
        throws IOException
    {
        Path repositoryPath = path.normalize();
        assertThat(repositoryPath, instanceOf(RepositoryPath.class));
        assertEquals(URI.create("strongbox:/storage0/mrpl-releases/org/bitbucket/b_c/jose4j/0.6.3/jose4j-0.6.3.jar"), repositoryPath.toUri());
    }

}