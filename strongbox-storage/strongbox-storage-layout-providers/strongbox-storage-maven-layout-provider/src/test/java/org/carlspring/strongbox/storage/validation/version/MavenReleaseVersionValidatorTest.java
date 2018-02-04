package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MockedMavenArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author stodorov
 */
public class MavenReleaseVersionValidatorTest
{

    Repository repository = new Repository();

    MavenReleaseVersionValidator validator = new MavenReleaseVersionValidator();


    @Before
    public void setUp()
            throws Exception
    {
        repository.setPolicy(RepositoryPolicyEnum.RELEASE.toString());
        repository.setLayout(RepositoryLayoutEnum.MAVEN_2.getLayout());
    }

    @Test
    public void shouldSupportRepository()
            throws VersionValidationException
    {
        assertTrue(validator.supports(repository));
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

        validator.validate(repository, coordinates1);
        validator.validate(repository, coordinates2);

        // If we've gotten here without an exception, then things are alright.
    }

    @Test
    public void testInvalidArtifacts()
    {
        /**
         * Test invalid artifacts
         */
        Artifact invalidArtifact1 = generateArtifact("1.0-SNAPSHOT");
        Artifact invalidArtifact2 = generateArtifact("1.0-20131004");
        Artifact invalidArtifact3 = generateArtifact("1.0-20131004.115330");
        Artifact invalidArtifact4 = generateArtifact("1.0-20131004.115330-1");

        ArtifactCoordinates coordinates1 = new MockedMavenArtifactCoordinates(invalidArtifact1);
        ArtifactCoordinates coordinates2 = new MockedMavenArtifactCoordinates(invalidArtifact2);
        ArtifactCoordinates coordinates3 = new MockedMavenArtifactCoordinates(invalidArtifact3);
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
            validator.validate(repository, coordinates2);
            fail("Incorrectly validated artifact with version 1.0-20131004!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(repository, coordinates3);
            fail("Incorrectly validated artifact with version 1.0-20131004.115330!");
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
        return new DefaultArtifact("org.carlspring.maven",
                                   "my-maven-plugin",
                                   version,
                                   "compile",
                                   "jar",
                                   null,
                                   new DefaultArtifactHandler("jar"));
    }

}
