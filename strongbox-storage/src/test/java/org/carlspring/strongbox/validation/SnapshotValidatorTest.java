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
public class SnapshotValidatorTest
{

    @Test
    public void testSnapshotValidation()
    {
        Repository repository = new Repository("Repository Name");
        SnapshotValidator validator = new SnapshotValidator();

        Artifact validArtifact1 = generateArtifact("1.0-SNAPSHOT");
        Artifact validArtifact2 = generateArtifact("1.0-20131004");
        Artifact validArtifact3 = generateArtifact("1.0-20131004.115330");
        Artifact validArtifact4 = generateArtifact("1.0-20131004.115330-1");

        assertTrue("Expected true, got false when validating artifact 1.0-SNAPSHOT",
                   validator.validate(repository, validArtifact1));
        assertTrue("Expected true, got false when validating artifact 1.0-20131004",
                   validator.validate(repository, validArtifact2));
        assertTrue("Expected true, got false when validating artifact 1.0-20131004.115330",
                   validator.validate(repository, validArtifact3));
        assertTrue("Expected true, got false when validating artifact 1.0-20131004.115330-1",
                   validator.validate(repository, validArtifact4));

        Artifact invalidArtifact1 = generateArtifact("1");
        Artifact invalidArtifact2 = generateArtifact("1.0");

        assertFalse("Expected false, got true when validating artifact with 1 as version",
                    validator.validate(repository, invalidArtifact1));
        assertFalse("Expected false, got true when validating artifact with 1.0 as version",
                    validator.validate(repository, invalidArtifact2));
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
