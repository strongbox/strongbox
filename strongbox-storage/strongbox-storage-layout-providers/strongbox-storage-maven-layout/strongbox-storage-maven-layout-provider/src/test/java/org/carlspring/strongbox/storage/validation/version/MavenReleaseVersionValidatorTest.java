package org.carlspring.strongbox.storage.validation.version;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MockedMavenArtifactCoordinates;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

/**
 * @author stodorov
 */
@Execution(CONCURRENT)
public class MavenReleaseVersionValidatorTest
{

    private MutableRepository repository;

    private MavenReleaseVersionValidator validator = new MavenReleaseVersionValidator();


    @BeforeEach
    public void setUp()
    {
        repository = new MutableRepository("mrvv-releases");
        repository.setPolicy(RepositoryPolicyEnum.RELEASE.toString());
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setBasedir("");
    }

    @Test
    public void shouldSupportRepository()
    {
        assertTrue(validator.supports(new ImmutableRepository(repository)));
    }

    @Test
    public void testReleaseValidation()
            throws VersionValidationException
    {
        /**
         * Test valid artifacts
         */
        Artifact validArtifact1 = generateArtifact("1");
        Artifact validArtifact2 = generateArtifact("1.0");

        ArtifactCoordinates coordinates1 = new MockedMavenArtifactCoordinates(validArtifact1);
        ArtifactCoordinates coordinates2 = new MockedMavenArtifactCoordinates(validArtifact2);

        validator.validate(new ImmutableRepository(repository), coordinates1);
        validator.validate(new ImmutableRepository(repository), coordinates2);

        // If we've gotten here without an exception, then things are alright.
    }

    @Test
    public void testInvalidArtifacts()
    {
        /**
         * Test invalid artifacts
         */
        Artifact invalidArtifact1 = generateArtifact("1.0-SNAPSHOT");
        Artifact invalidArtifact3 = generateArtifact("1.0-20131004.115330");
        Artifact invalidArtifact4 = generateArtifact("1.0-20131004.115330-1");

        ArtifactCoordinates coordinates1 = new MockedMavenArtifactCoordinates(invalidArtifact1);
        ArtifactCoordinates coordinates3 = new MockedMavenArtifactCoordinates(invalidArtifact3);
        ArtifactCoordinates coordinates4 = new MockedMavenArtifactCoordinates(invalidArtifact4);

        try
        {
            validator.validate(new ImmutableRepository(repository), coordinates1);
            fail("Incorrectly validated artifact with version 1.0-SNAPSHOT!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(new ImmutableRepository(repository), coordinates3);
            fail("Incorrectly validated artifact with version 1.0-20131004.115330!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(new ImmutableRepository(repository), coordinates4);
            fail("Incorrectly validated artifact with version 1.0-20131004.115330-1!");
        }
        catch (VersionValidationException e)
        {
        }
    }

    private Artifact generateArtifact(String version)
    {
        return new DefaultArtifact("org.carlspring.maven",
                                   "my-maven-plugin",
                                   version,
                                   "compile",
                                   "jar",
                                   null,
                                   new DefaultArtifactHandler("jar"));
    }

}
