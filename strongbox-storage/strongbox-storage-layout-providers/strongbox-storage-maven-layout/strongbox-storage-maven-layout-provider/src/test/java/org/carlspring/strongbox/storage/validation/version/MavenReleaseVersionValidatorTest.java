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
public class MavenReleaseVersionValidatorTest
{

    private static final String GROUP_ID = "org.carlspring.maven";
    private static final String ARTIFACT_ID = "my-maven-plugin";

    private MavenReleaseVersionValidator validator = new MavenReleaseVersionValidator();

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void shouldSupportRepository(@MavenRepository(repositoryId = "mrvv-releases-ssr") Repository repository)
    {
        assertThat(validator.supports(repository)).isTrue();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testReleaseValidation(@MavenRepository(repositoryId = "mrvv-releases-trv") Repository repository,
                                      @MavenTestArtifact(repositoryId = "mrvv-releases-trv",
                                                         id = GROUP_ID + ":" + ARTIFACT_ID,
                                                         versions = { "1",
                                                                      "1.0" })
                                      List<Path> validArtifactPaths)
            throws VersionValidationException, IOException
    {
        /*
         * Test valid artifacts
         */
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
    public void testInvalidArtifacts(@MavenRepository(repositoryId = "mrvv-releases-tia") Repository repository,
                                     @MavenTestArtifact(repositoryId = "mrvv-releases-tia",
                                                        id = GROUP_ID + ":" + ARTIFACT_ID,
                                                        versions = { "1.0-SNAPSHOT",
                                                                     "1.0-20131004.115330-1" })
                                     List<Path> invalidArtifactPaths)
            throws IOException
    {
        /*
         * Test invalid artifacts
         */
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
