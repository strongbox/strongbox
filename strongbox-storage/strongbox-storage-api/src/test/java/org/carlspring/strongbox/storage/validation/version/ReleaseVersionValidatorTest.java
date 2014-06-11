package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.fail;

/**
 * @author stodorov
 */
public class ReleaseVersionValidatorTest
{

    Repository repository = new Repository();

    ReleaseVersionValidator validator = new ReleaseVersionValidator();


    @Before
    public void setUp()
            throws Exception
    {
        repository.setPolicy(RepositoryPolicyEnum.RELEASE.toString());
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

        validator.validate(repository, validArtifact1);
        validator.validate(repository, validArtifact2);

        // If we've gotten here without an exception, then things are alright.
    }

    @Test
    public void testInvalidArtifacts()
    {
        /**
         * Test invalid artifacts
         */
        Artifact invalidArtifact3 = generateArtifact("1.0-SNAPSHOT");
        Artifact invalidArtifact4 = generateArtifact("1.0-20131004");
        Artifact invalidArtifact5 = generateArtifact("1.0-20131004.115330");
        Artifact invalidArtifact6 = generateArtifact("1.0-20131004.115330-1");

        try
        {
            validator.validate(repository, invalidArtifact3);
            fail("Incorrectly validated artifact with version 1.0-SNAPSHOT!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(repository, invalidArtifact4);
            fail("Incorrectly validated artifact with version 1.0-20131004!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(repository, invalidArtifact5);
            fail("Incorrectly validated artifact with version 1.0-20131004.115330!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(repository, invalidArtifact6);
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
