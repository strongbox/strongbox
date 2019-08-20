package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.net.URI;
import java.nio.file.Path;

import com.google.inject.Inject;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenArtifactTest
{

    private static final String REPOSITORY_RELEASES = "mrpl-releases";

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    void artifactWithUnderscoreShouldWork(@MavenRepository(repositoryId = REPOSITORY_RELEASES)
                                          Repository repository,
                                          @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                             id = "org.bitbucket.b_c:jose4j",
                                                             versions = "0.6.3")
                                          Path path)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        final RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        Path artifactRepositoryPath = path.normalize();
        assertThat(artifactRepositoryPath, instanceOf(RepositoryPath.class));

        final String artifactRepositoryPathStr = FilenameUtils.separatorsToUnix(
                repositoryPath.relativize(artifactRepositoryPath).toString());

        String jarPath = String.format("strongbox:/%s/%s/%s", storageId, repositoryId, artifactRepositoryPathStr);
        assertEquals(URI.create(jarPath), artifactRepositoryPath.toUri());
    }

}
