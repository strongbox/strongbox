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
import static org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum.SNAPSHOT;
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
public class MavenSnapshotVersionValidatorTest
{

    private static final String REPOSITORY_ID = "test-repository-for-maven-snapshot-validation";
    private static final String GROUP_ID = "org.carlspring.maven";
    private static final String ARTIFACT_ID = "my-maven-plugin";

    private MavenSnapshotVersionValidator validator = new MavenSnapshotVersionValidator();

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void shouldSupportRepository(@MavenRepository(repositoryId = REPOSITORY_ID, policy = SNAPSHOT) 
                                        Repository repository)
    {
        assertTrue(validator.supports(repository));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testSnapshotValidation(@MavenRepository(repositoryId = REPOSITORY_ID, policy = SNAPSHOT)
                                       Repository repository,
                                       @MavenTestArtifact(repositoryId = REPOSITORY_ID,
                                                          id = GROUP_ID + ":" + ARTIFACT_ID,
                                                          versions = { "1.0-SNAPSHOT" })
                                       Path validArtifact1Path,
                                       @MavenTestArtifact(repositoryId = REPOSITORY_ID,
                                                          id = GROUP_ID + ":" + ARTIFACT_ID,
                                                          versions = { "1.0-20131004.115330-1" })
                                       Path validArtifact4Path,
                                       @MavenTestArtifact(repositoryId = REPOSITORY_ID,
                                                          id = GROUP_ID + ":" + ARTIFACT_ID,
                                                          versions = { "1.0.8-20151025.032208-1" })
                                       Path validArtifact5Path,
                                       @MavenTestArtifact(repositoryId = REPOSITORY_ID,
                                                          id = GROUP_ID + ":" + ARTIFACT_ID,
                                                          versions = { "1.0.8-alpha-1-20151025.032208-1" })
                                       Path validArtifact6Path)
            throws VersionValidationException
    {
        Artifact validArtifact1 = generateArtifact("1.0-SNAPSHOT");
        Artifact validArtifact4 = generateArtifact("1.0-20131004.115330-1");
        Artifact validArtifact5 = generateArtifact("1.0.8-20151025.032208-1");
        Artifact validArtifact6 = generateArtifact("1.0.8-alpha-1-20151025.032208-1");

        ArtifactCoordinates coordinates1 = new MockedMavenArtifactCoordinates(validArtifact1);
        ArtifactCoordinates coordinates4 = new MockedMavenArtifactCoordinates(validArtifact4);
        ArtifactCoordinates coordinates5 = new MockedMavenArtifactCoordinates(validArtifact5);
        ArtifactCoordinates coordinates6 = new MockedMavenArtifactCoordinates(validArtifact6);

        validator.validate(repository, coordinates1);
        validator.validate(repository, coordinates4);
        validator.validate(repository, coordinates5);
        validator.validate(repository, coordinates6);

        // If we've gotten here without an exception, then things are alright.
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testInvalidArtifacts(@MavenRepository(repositoryId = REPOSITORY_ID, policy = SNAPSHOT)
                                     Repository repository,
                                     @MavenTestArtifact(repositoryId = REPOSITORY_ID,
                                                        id = GROUP_ID + ":" + ARTIFACT_ID,
                                                        versions = { "1" })
                                     Path invalidArtifact1Path,
                                     @MavenTestArtifact(repositoryId = REPOSITORY_ID,
                                                        id = GROUP_ID + ":" + ARTIFACT_ID,
                                                        versions = { "1.0" })
                                     Path invalidArtifact2Path,
                                     @MavenTestArtifact(repositoryId = REPOSITORY_ID,
                                                        id = GROUP_ID + ":" + ARTIFACT_ID,
                                                        versions = { "1.0.1" })
                                     Path invalidArtifact3Path,
                                     @MavenTestArtifact(repositoryId = REPOSITORY_ID,
                                                        id = GROUP_ID + ":" + ARTIFACT_ID,
                                                        versions = { "1.0.1-alpha" })
                                     Path invalidArtifact4Path)
    {
        Artifact invalidArtifact1 = generateArtifact("1");
        Artifact invalidArtifact2 = generateArtifact("1.0");
        Artifact invalidArtifact3 = generateArtifact("1.0.1");
        Artifact invalidArtifact4 = generateArtifact("1.0.1-alpha");

        ArtifactCoordinates coordinates1 = new MockedMavenArtifactCoordinates(invalidArtifact1);
        ArtifactCoordinates coordinates2 = new MockedMavenArtifactCoordinates(invalidArtifact2);
        ArtifactCoordinates coordinates3 = new MockedMavenArtifactCoordinates(invalidArtifact3);
        ArtifactCoordinates coordinates4 = new MockedMavenArtifactCoordinates(invalidArtifact4);

        try
        {
            validator.validate(repository, coordinates1);

            fail("Incorrectly validated artifact with version 1!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(repository, coordinates2);

            fail("Incorrectly validated artifact with version 1.0!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(repository, coordinates3);

            fail("Incorrectly validated artifact with version 1.0.1!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(repository, coordinates4);

            fail("Incorrectly validated artifact with version 1.0.1!");
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
