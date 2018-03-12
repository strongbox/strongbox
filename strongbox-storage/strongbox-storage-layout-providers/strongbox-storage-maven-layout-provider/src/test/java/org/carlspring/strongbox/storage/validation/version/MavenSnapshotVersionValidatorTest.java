package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenDetachedArtifact;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.MockedMavenArtifactCoordinates;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author stodorov
 */
public class MavenSnapshotVersionValidatorTest
{

    Repository repository;

    MavenSnapshotVersionValidator validator = new MavenSnapshotVersionValidator();


    @Before
    public void setUp()
    {
        repository = new Repository("test-repository-for-maven-snapshot-validation");
        repository.setPolicy(RepositoryPolicyEnum.SNAPSHOT.toString());
        repository.setLayout(Maven2LayoutProvider.ALIAS);
    }

    @Test
    public void shouldSupportRepository()
    {
        assertTrue(validator.supports(repository));
    }

    @Test
    public void testSnapshotValidation()
            throws VersionValidationException
    {
        MavenArtifact validArtifact1 = generateArtifact("1.0-SNAPSHOT");
        MavenArtifact validArtifact2 = generateArtifact("1.0-20131004");
        MavenArtifact validArtifact3 = generateArtifact("1.0-20131004.115330");
        MavenArtifact validArtifact4 = generateArtifact("1.0-20131004.115330-1");
        MavenArtifact validArtifact5 = generateArtifact("1.0.8-20151025.032208-1");
        MavenArtifact validArtifact6 = generateArtifact("1.0.8-alpha-1-20151025.032208-1");

        ArtifactCoordinates coordinates1 = new MockedMavenArtifactCoordinates(validArtifact1);
        ArtifactCoordinates coordinates2 = new MockedMavenArtifactCoordinates(validArtifact2);
        ArtifactCoordinates coordinates3 = new MockedMavenArtifactCoordinates(validArtifact3);
        ArtifactCoordinates coordinates4 = new MockedMavenArtifactCoordinates(validArtifact4);
        ArtifactCoordinates coordinates5 = new MockedMavenArtifactCoordinates(validArtifact5);
        ArtifactCoordinates coordinates6 = new MockedMavenArtifactCoordinates(validArtifact6);

        validator.validate(repository, coordinates1);
        validator.validate(repository, coordinates2);
        validator.validate(repository, coordinates3);
        validator.validate(repository, coordinates4);
        validator.validate(repository, coordinates5);
        validator.validate(repository, coordinates6);

        // If we've gotten here without an exception, then things are alright.
    }

    @Test
    public void testInvalidArtifacts()
    {
        MavenArtifact invalidArtifact1 = generateArtifact("1");
        MavenArtifact invalidArtifact2 = generateArtifact("1.0");
        MavenArtifact invalidArtifact3 = generateArtifact("1.0.1");
        MavenArtifact invalidArtifact4 = generateArtifact("1.0.1-alpha");

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

    private MavenArtifact generateArtifact(String version)
    {
        return new MavenDetachedArtifact("org.carlspring.maven",
                                         "my-maven-plugin",
                                         version,
                                         "jar",
                                         null);
    }

}
