package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.NugetHierarchicalArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.fail;

public class NugetReleaseVersionValidatorTest
{

    Repository repository = new Repository();

    NugetReleaseVersionValidator validator = new NugetReleaseVersionValidator();

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
        ArtifactCoordinates coordinates1 = new NugetHierarchicalArtifactCoordinates();
        coordinates1.setVersion("1");

        ArtifactCoordinates coordinates2 = new NugetHierarchicalArtifactCoordinates();
        coordinates2.setVersion("1.0");

        ArtifactCoordinates coordinates3 = new NugetHierarchicalArtifactCoordinates();
        coordinates3.setVersion("1.0-RELEASE");

        try
        {
            validator.validate(repository, coordinates1);
            validator.validate(repository, coordinates2);
            validator.validate(repository, coordinates3);
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
        ArtifactCoordinates coordinates1 = new NugetHierarchicalArtifactCoordinates();
        coordinates1.setVersion("1.0-SNAPSHOT");

        validator.validate(repository, coordinates1);
    }

}
