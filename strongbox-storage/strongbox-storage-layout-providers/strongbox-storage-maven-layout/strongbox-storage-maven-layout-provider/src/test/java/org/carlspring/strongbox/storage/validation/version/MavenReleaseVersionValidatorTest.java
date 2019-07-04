package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MockedMavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Path;

import org.apache.maven.artifact.Artifact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
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

    private static final String MRVV_RELEASES = "mrvv-releases";
    private static final String GROUP_ID = "org.carlspring.maven";
    private static final String ARTIFACT_ID = "my-maven-plugin";

    private MavenReleaseVersionValidator validator = new MavenReleaseVersionValidator();

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void shouldSupportRepository(@MavenRepository(repositoryId = MRVV_RELEASES) Repository repository)
    {
        assertTrue(validator.supports(repository));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testReleaseValidation(@MavenRepository(repositoryId = MRVV_RELEASES) Repository repository,
                                      @MavenTestArtifact(repositoryId = MRVV_RELEASES,
                                                         id = GROUP_ID + ":" + ARTIFACT_ID,
                                                         versions = { "1" })
                                      Path validArtifact1Path,
                                      @MavenTestArtifact(repositoryId = MRVV_RELEASES,
                                                         id = GROUP_ID + ":" + ARTIFACT_ID,
                                                         versions = { "1.0" })
                                      Path validArtifact2Path)
            throws VersionValidationException
    {
        /*
         * Test valid artifacts
         */
        Artifact validArtifact1 = generateArtifact("1");
        Artifact validArtifact2 = generateArtifact("1.0");

        ArtifactCoordinates coordinates1 = new MockedMavenArtifactCoordinates(validArtifact1);
        ArtifactCoordinates coordinates2 = new MockedMavenArtifactCoordinates(validArtifact2);

        validator.validate(repository, coordinates1);
        validator.validate(repository, coordinates2);

        // If we've gotten here without an exception, then things are alright.
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testInvalidArtifacts(@MavenRepository(repositoryId = MRVV_RELEASES) Repository repository,
                                     @MavenTestArtifact(repositoryId = MRVV_RELEASES,
                                                        id = GROUP_ID + ":" + ARTIFACT_ID,
                                                        versions = {"1.0-SNAPSHOT"})
                                     Path invalidArtifact1Path,
                                     @MavenTestArtifact(repositoryId = MRVV_RELEASES,
                                                        id = GROUP_ID + ":" + ARTIFACT_ID,
                                                        versions = {"1.0-20131004.115330-1"})
                                     Path invalidArtifact4Path)
    {
        /*
         * Test invalid artifacts
         */
        Artifact invalidArtifact1 = generateArtifact("1.0-SNAPSHOT");
        Artifact invalidArtifact4 = generateArtifact("1.0-20131004.115330-1");

        ArtifactCoordinates coordinates1 = new MockedMavenArtifactCoordinates(invalidArtifact1);
        ArtifactCoordinates coordinates4 = new MockedMavenArtifactCoordinates(invalidArtifact4);

        try
        {
            validator.validate(repository, coordinates1);
            fail("Incorrectly validated artifact with version 1.0-SNAPSHOT!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(repository, coordinates4);
            fail("Incorrectly validated artifact with version 1.0-20131004.115330-1!");
        }
        catch (VersionValidationException e)
        {
        }
    }

    private Artifact generateArtifact(String version)
    {
        String gavtc = String.format("%s:%s:%s:jar", GROUP_ID, ARTIFACT_ID, version);
        return MavenArtifactTestUtils.getArtifactFromGAVTC(gavtc);
    }
}
