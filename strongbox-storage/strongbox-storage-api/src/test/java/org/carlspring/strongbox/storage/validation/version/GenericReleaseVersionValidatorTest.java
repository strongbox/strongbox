package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MockedMavenArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.validation.artifact.version.GenericReleaseVersionValidator;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class GenericReleaseVersionValidatorTest
{

    Repository repository;

    GenericReleaseVersionValidator validator = new GenericReleaseVersionValidator();


    @BeforeEach
    public void setUp()
    {
        MutableRepository repository = new MutableRepository("test-repository-for-nuget-release-validation");
        repository.setPolicy(RepositoryPolicyEnum.RELEASE.toString());
        repository.setBasedir("");
        this.repository = new Repository(repository);
    }

    @Test
    public void testReleaseValidation()
    {
        ArtifactCoordinates coordinates1 = new MockedMavenArtifactCoordinates();
        coordinates1.setVersion("1");

        ArtifactCoordinates coordinates2 = new MockedMavenArtifactCoordinates();
        coordinates2.setVersion("1.0");

        ArtifactCoordinates coordinates3 = new MockedMavenArtifactCoordinates();
        coordinates3.setVersion("1.0-rc-1");

        ArtifactCoordinates coordinates4 = new MockedMavenArtifactCoordinates();
        coordinates4.setVersion("1.0-milestone-1");

        ArtifactCoordinates coordinates5 = new MockedMavenArtifactCoordinates();
        coordinates5.setVersion("1.0-alpha-1");

        ArtifactCoordinates coordinates6 = new MockedMavenArtifactCoordinates();
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

    @Test
    public void testInvalidArtifacts()
            throws VersionValidationException
    {
        ArtifactCoordinates coordinates1 = new MockedMavenArtifactCoordinates();
        coordinates1.setVersion("1.0-SNAPSHOT");

        assertThrows(VersionValidationException.class, () -> {
            validator.validate(repository, coordinates1);
        });
    }

}
