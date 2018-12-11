package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MockedMavenArtifactCoordinates;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author stodorov
 */
@Execution(CONCURRENT)
public class MavenSnapshotVersionValidatorTest
{

    MutableRepository repository;

    MavenSnapshotVersionValidator validator = new MavenSnapshotVersionValidator();


    @BeforeEach
    public void setUp()
    {
        repository = new MutableRepository("test-repository-for-maven-snapshot-validation");
        repository.setPolicy(RepositoryPolicyEnum.SNAPSHOT.toString());
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setBasedir("");
    }

    @Test
    public void shouldSupportRepository()
    {
        assertTrue(validator.supports(new Repository(repository)));
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

        ArtifactCoordinates coordinates1 = new MockedMavenArtifactCoordinates(validArtifact1);
        ArtifactCoordinates coordinates2 = new MockedMavenArtifactCoordinates(validArtifact2);
        ArtifactCoordinates coordinates3 = new MockedMavenArtifactCoordinates(validArtifact3);
        ArtifactCoordinates coordinates4 = new MockedMavenArtifactCoordinates(validArtifact4);
        ArtifactCoordinates coordinates5 = new MockedMavenArtifactCoordinates(validArtifact5);
        ArtifactCoordinates coordinates6 = new MockedMavenArtifactCoordinates(validArtifact6);

        validator.validate(new Repository(repository), coordinates1);
        validator.validate(new Repository(repository), coordinates2);
        validator.validate(new Repository(repository), coordinates3);
        validator.validate(new Repository(repository), coordinates4);
        validator.validate(new Repository(repository), coordinates5);
        validator.validate(new Repository(repository), coordinates6);

        // If we've gotten here without an exception, then things are alright.
    }

    @Test
    public void testInvalidArtifacts()
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
            validator.validate(new Repository(repository), coordinates1);

            fail("Incorrectly validated artifact with version 1!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(new Repository(repository), coordinates2);

            fail("Incorrectly validated artifact with version 1.0!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(new Repository(repository), coordinates3);

            fail("Incorrectly validated artifact with version 1.0.1!");
        }
        catch (VersionValidationException e)
        {
        }

        try
        {
            validator.validate(new Repository(repository), coordinates4);

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
