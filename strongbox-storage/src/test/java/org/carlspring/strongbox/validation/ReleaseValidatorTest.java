package org.carlspring.strongbox.validation;

import org.carlspring.strongbox.storage.repository.Repository;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import static junit.framework.Assert.assertFalse;
import org.junit.Test;

import org.apache.maven.artifact.Artifact;

import static junit.framework.Assert.assertTrue;

/**
 * @author stodorov
 */
public class ReleaseValidatorTest
{

    @Test
    public void testReleaseValidation()
    {
        Repository repository = new Repository();
        ReleaseValidator validator = new ReleaseValidator();

        /**
         * Test valid artifacts
         */
        Artifact validArtifact1 = generateArtifact("1");
        Artifact validArtifact2 = generateArtifact("1.0");

        assertTrue("Expected true, got false when validating artifact", validator.validate(repository, validArtifact1));
        assertTrue("Expected true, got false when validating artifact", validator.validate(repository, validArtifact2));

        /**
         * Test invalid artifacts
         */
        Artifact invalidArtifact1 = generateArtifact("");
        Artifact invalidArtifact2 = generateArtifact(" ");
        Artifact invalidArtifact3 = generateArtifact("1.0-SNAPSHOT");
        Artifact invalidArtifact4 = generateArtifact("1.0-20131004");
        Artifact invalidArtifact5 = generateArtifact("1.0-20131004.115330");
        Artifact invalidArtifact6 = generateArtifact("1.0-20131004.115330-1");

        assertFalse("Expected false, got true when validating artifact with empty string as version",
                    validator.validate(repository, invalidArtifact1));
        assertFalse("Expected false, got true when validating artifact with whitespace string as version",
                    validator.validate(repository, invalidArtifact2));
        assertFalse("Expected false, got true when validating artifact with 1.0-SNAPSHOT as version",
                    validator.validate(repository, invalidArtifact3));
        assertFalse("Expected false, got true when validating artifact with 1.0-20131004 as version",
                    validator.validate(repository, invalidArtifact4));
        assertFalse("Expected false, got true when validating artifact with 1.0-20131004.115330 as version",
                    validator.validate(repository, invalidArtifact5));
        assertFalse("Expected false, got true when validating artifact with 1.0-20131004.115330-1 as version",
                    validator.validate(repository, invalidArtifact6));
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
