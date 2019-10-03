package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum.SNAPSHOT;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author stodorov
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenSnapshotVersionValidatorTest
{

    private static final String REPOSITORY_ID_1 = "test-repository-for-maven-snapshot-validation-1";
    private static final String REPOSITORY_ID_2 = "test-repository-for-maven-snapshot-validation-2";
    private static final String REPOSITORY_ID_3 = "test-repository-for-maven-snapshot-validation-3";
    private static final String GROUP_ID = "org.carlspring.maven";
    private static final String ARTIFACT_ID = "my-maven-plugin";

    private MavenSnapshotVersionValidator validator = new MavenSnapshotVersionValidator();

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void shouldSupportRepository(@MavenRepository(repositoryId = REPOSITORY_ID_1, policy = SNAPSHOT)
                                        Repository repository)
    {
        assertThat(validator.supports(repository)).isTrue();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testSnapshotValidation(@MavenRepository(repositoryId = REPOSITORY_ID_2, policy = SNAPSHOT)
                                       Repository repository,
                                       @MavenTestArtifact(repositoryId = REPOSITORY_ID_2,
                                                          id = GROUP_ID + ":" + ARTIFACT_ID,
                                                          versions = { "1.0-SNAPSHOT",
                                                                       "1.0-20131004.115330-1",
                                                                       "1.0.8-20151025.032208-1",
                                                                       "1.0.8-alpha-1-20151025.032208-1"})
                                       List<Path> validArtifactPaths)
            throws VersionValidationException, IOException
    {
        for (Path validArtifactPath : validArtifactPaths)
        {
            RepositoryPath repositoryPath = (RepositoryPath) validArtifactPath.normalize();
            ArtifactCoordinates coordinates = RepositoryFiles.readCoordinates(repositoryPath);
            validator.validate(repository, coordinates);
        }

        // If we've gotten here without an exception, then things are alright.
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testInvalidArtifacts(@MavenRepository(repositoryId = REPOSITORY_ID_3, policy = SNAPSHOT)
                                     Repository repository,
                                     @MavenTestArtifact(repositoryId = REPOSITORY_ID_3,
                                                        id = GROUP_ID + ":" + ARTIFACT_ID,
                                                        versions = { "1",
                                                                     "1.0",
                                                                     "1.0.1",
                                                                     "1.0.1-alpha" })
                                     List<Path> invalidArtifactPaths)
            throws IOException
    {
        for (Path invalidArtifactPath : invalidArtifactPaths)
        {
            RepositoryPath repositoryPath = (RepositoryPath) invalidArtifactPath.normalize();
            ArtifactCoordinates coordinates = RepositoryFiles.readCoordinates(repositoryPath);

            try
            {
                validator.validate(repository, coordinates);
                fail("Incorrectly validated artifact with version " + coordinates.getVersion());
            }
            catch (VersionValidationException e)
            {
            }
        }
    }

}
