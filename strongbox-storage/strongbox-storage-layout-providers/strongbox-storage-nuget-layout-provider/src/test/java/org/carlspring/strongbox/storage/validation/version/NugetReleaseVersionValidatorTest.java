package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.validation.artifact.version.GenericReleaseVersionValidator;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.fail;

public class NugetReleaseVersionValidatorTest
{

    Repository repository = new Repository("test-repository-for-nuget-release-validation");

    GenericReleaseVersionValidator validator = new GenericReleaseVersionValidator();

    @Before
    public void setUp()
    {
        repository.setPolicy(RepositoryPolicyEnum.RELEASE.toString());
        repository.setLayout(RepositoryLayoutEnum.NUGET.getLayout());
    }

    @Test
    public void testReleaseValidation()
            throws VersionValidationException {
        ArtifactCoordinates coordinates1 = new NugetArtifactCoordinates();
        coordinates1.setVersion("1");

        ArtifactCoordinates coordinates2 = new NugetArtifactCoordinates();
        coordinates2.setVersion("1.0");

        ArtifactCoordinates coordinates3 = new NugetArtifactCoordinates();
        coordinates3.setVersion("1.0-rc-1");

        ArtifactCoordinates coordinates4 = new NugetArtifactCoordinates();
        coordinates4.setVersion("1.0-milestone-1");

        ArtifactCoordinates coordinates5 = new NugetArtifactCoordinates();
        coordinates5.setVersion("1.0-alpha-1");

        ArtifactCoordinates coordinates6 = new NugetArtifactCoordinates();
        coordinates6.setVersion("1.0-beta-1");

        try
        {
            validator.validate(repository, coordinates1);
            validator.validate(repository, coordinates2);
            validator.validate(repository, coordinates3);
            validator.validate(repository, coordinates4);
            validator.validate(repository, coordinates5);
            validator.validate(repository, coordinates6);
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
        coordinates1.setVersion("1.0-SNAPSHOT");

        validator.validate(repository, coordinates1);
    }

}
