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
public class SnapshotVersionValidatorTest
{

    Repository repository = new Repository();

    SnapshotVersionValidator validator = new SnapshotVersionValidator();


    @Before
    public void setUp()
            throws Exception
    {
        repository.setPolicy(RepositoryPolicyEnum.SNAPSHOT.toString());
    }

    @Test
    public void testSnapshotValidation()
            throws VersionValidationException
    {
        Artifact validArtifact1 = generateArtifact("1.0-SNAPSHOT");
        Artifact validArtifact2 = generateArtifact("1.0-20131004");
        Artifact validArtifact3 = generateArtifact("1.0-20131004.115330");
        Artifact validArtifact4 = generateArtifact("1.0-20131004.115330-1");
        Artifact validArtifact5 = generateArtifact("1.0.8-20151025.032208-1");
        Artifact validArtifact6 = generateArtifact("1.0.8-alpha-1-20151025.032208-1");

        validator.validate(repository, validArtifact1);
        validator.validate(repository, validArtifact2);
        validator.validate(repository, validArtifact3);
        validator.validate(repository, validArtifact4);
        validator.validate(repository, validArtifact5);
        validator.validate(repository, validArtifact6);

        // If we've gotten here without an exception, then things are alright.
    }

    @Test
    public void testInvalidArtifacts()
    {
        Artifact invalidArtifact1 = generateArtifact("1");
        Artifact invalidArtifact2 = generateArtifact("1.0");
        Artifact invalidArtifact3 = generateArtifact("1.0.1");
        Artifact invalidArtifact4 = generateArtifact("1.0.1-alpha");

        try
        {
            validator.validate(repository, invalidArtifact1);
            fail("Incorrectly validated artifact with version 1!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(repository, invalidArtifact2);
            fail("Incorrectly validated artifact with version 1.0!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(repository, invalidArtifact3);
            fail("Incorrectly validated artifact with version 1.0.1!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(repository, invalidArtifact4);
            fail("Incorrectly validated artifact with version 1.0.1!");
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
