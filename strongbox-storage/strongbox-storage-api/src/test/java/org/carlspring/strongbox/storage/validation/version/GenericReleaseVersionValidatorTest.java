package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MockedMavenArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.validation.artifact.version.GenericReleaseVersionValidator;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

public class GenericReleaseVersionValidatorTest
{

    private Repository repository;

    private GenericReleaseVersionValidator validator = new GenericReleaseVersionValidator();


    @BeforeEach
    public void setUp()
    {
        RepositoryDto repository = new RepositoryDto("test-repository-for-nuget-release-validation");
        repository.setPolicy(RepositoryPolicyEnum.RELEASE.toString());
        repository.setBasedir("");
        this.repository = new RepositoryData(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = { "1",
                             "1.0",
                             "1.0-rc-1",
                             "1.0-milestone-1",
                             "1.0-alpha-1",
                             "1.0-beta-1" })
    public void testReleaseValidation(final String version)
    {
        MockedMavenArtifactCoordinates coordinates = new MockedMavenArtifactCoordinates();
        coordinates.setVersion(version);

        try
        {
            validator.validate(repository, coordinates);
        }
        catch (Exception ex)
        {
            fail("Validator should not throw any exception but received " + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Test
    public void testInvalidArtifacts()
    {
        MockedMavenArtifactCoordinates coordinates1 = new MockedMavenArtifactCoordinates();
        coordinates1.setVersion("1.0-SNAPSHOT");

        assertThatExceptionOfType(VersionValidationException.class)
                .isThrownBy(() -> validator.validate(repository, coordinates1));
    }

}
