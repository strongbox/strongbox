package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MockedMavenArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.validation.artifact.version.GenericSnapshotVersionValidator;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.fail;

public class GenericSnapshotVersionValidatorTest
{

    Repository repository;

    GenericSnapshotVersionValidator validator = new GenericSnapshotVersionValidator();


    @Before
    public void setUp()
    {
        repository = new Repository("test-repository-for-nuget-release-validation");
        repository.setPolicy(RepositoryPolicyEnum.SNAPSHOT.toString());
        repository.setLayout(RepositoryLayoutEnum.NUGET.getLayout());
    }

    @Test
    public void testSnapshotValidation()
    {
        ArtifactCoordinates coordinates1 = new MockedMavenArtifactCoordinates();
        coordinates1.setVersion("1.0-SNAPSHOT");

        try
        {
            validator.validate(repository, coordinates1);
        }
        catch (Exception ex)
        {
            fail("Validator should not throw any exception but received " + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Test(expected = VersionValidationException.class)
    public void testInvalidArtifacts()
            throws VersionValidationException
    {
        ArtifactCoordinates coordinates1 = new MockedMavenArtifactCoordinates();
        coordinates1.setVersion("1.0");

        validator.validate(repository, coordinates1);
    }

}
