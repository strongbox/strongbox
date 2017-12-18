package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.fail;

public class NugetSnapshotVersionValidatorTest
{

    Repository repository = new Repository();

    NugetSnapshotVersionValidator validator = new NugetSnapshotVersionValidator();


    @Before
    public void setUp()
            throws Exception
    {
        repository.setPolicy(RepositoryPolicyEnum.SNAPSHOT.toString());
        repository.setLayout(RepositoryLayoutEnum.NUGET_HIERARCHICAL.getLayout());
    }

    @Test
    public void testSnapshotValidation()
            throws VersionValidationException
    {
        ArtifactCoordinates coordinates1 = new NugetArtifactCoordinates();
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
        ArtifactCoordinates coordinates1 = new NugetArtifactCoordinates();
        coordinates1.setVersion("1.0");

        validator.validate(repository, coordinates1);
    }

}
