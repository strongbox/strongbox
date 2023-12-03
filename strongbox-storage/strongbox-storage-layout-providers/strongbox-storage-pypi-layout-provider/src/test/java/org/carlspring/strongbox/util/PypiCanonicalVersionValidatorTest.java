package org.carlspring.strongbox.util;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MockedMavenArtifactCoordinates;
import org.carlspring.strongbox.config.PypiLayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;
import org.carlspring.strongbox.storage.validation.version.PypiVersionValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author sainalshah
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@Execution(CONCURRENT)
@ContextConfiguration(classes = PypiLayoutProviderTestConfig.class)
public class PypiCanonicalVersionValidatorTest
{

    @Autowired
    private PypiVersionValidator pypiCanonicalVersionValidator;

    private Repository repository;

    @BeforeEach
    public void setUp()
    {
        RepositoryDto repository = new RepositoryDto("test-repository-for-pypi-canonical-version-validation");
        repository.setBasedir("");
        this.repository = new RepositoryData(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.0.dev456.post123",
                             "rc1.0a1",
                             ".1",
                             "C1.0a12.dev456",
                             "v1.0a12",
                             "1.0b1.dev456.-7",
                             "1.0b212^",
                             "1.0+abc.5+154",
                             "1.0rc+abc.7",
                             "1~0.post456",
                             "1-1-dev1",
                             "1!1.4.rc" })
    public void testInvalidVersionCombinations(String version)
    {
        ArtifactCoordinates coordinates = new MockedMavenArtifactCoordinates();
        coordinates.setVersion(version);
        assertThatExceptionOfType(VersionValidationException.class)
                .isThrownBy(() -> pypiCanonicalVersionValidator.validate(repository, coordinates));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0.1",
                             "0.2",
                             "0.3",
                             "1.0",
                             "1.1",
                             "11.5" })
    public void testValidMajorMinorVersioning(String version)
    {
        ArtifactCoordinates coordinates = new MockedMavenArtifactCoordinates();
        coordinates.setVersion(version);
        assertThatCode(
                () -> pypiCanonicalVersionValidator.validate(repository, coordinates)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.1.0",
                             "1.1.1",
                             "1.1.2",
                             "1.2.0",
                             "39.6.3" })
    public void testValidMajorMinorMicroVersioning(String version)
    {
        ArtifactCoordinates coordinates = new MockedMavenArtifactCoordinates();
        coordinates.setVersion(version);
        assertThatCode(
                () -> pypiCanonicalVersionValidator.validate(repository, coordinates)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.0a1",
                             "1.0a2",
                             "1.0b1",
                             "1.0rc1",
                             "1.1a1",
                             "1.0c1",
                             "1.0c2" })
    public void testValidPreReleaseVersioning(String version)
    {
        ArtifactCoordinates coordinates = new MockedMavenArtifactCoordinates();
        coordinates.setVersion(version);
        assertThatCode(
                () -> pypiCanonicalVersionValidator.validate(repository, coordinates)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.0.post1",
                             "1.3.post2",
                             "1.0.post12" })
    public void testValidPostReleaseVersioning(String version)
    {
        ArtifactCoordinates coordinates = new MockedMavenArtifactCoordinates();
        coordinates.setVersion(version);
        assertThatCode(
                () -> pypiCanonicalVersionValidator.validate(repository, coordinates)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.0.dev1",
                             "1.0.dev2",
                             "1.0.dev0",
                             "1.0.dev4",
                             "1.4.1.dev1",
                             "26.1.dev1" })
    public void testValidDevReleaseVersioning(String version)
    {
        ArtifactCoordinates coordinates = new MockedMavenArtifactCoordinates();
        coordinates.setVersion(version);
        assertThatCode(
                () -> pypiCanonicalVersionValidator.validate(repository, coordinates)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "2012.15",
                             "2013.1",
                             "2013.2" })
    public void testValidDateBasedVersioning(String version)
    {
        ArtifactCoordinates coordinates = new MockedMavenArtifactCoordinates();
        coordinates.setVersion(version);
        assertThatCode(
                () -> pypiCanonicalVersionValidator.validate(repository, coordinates)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.5+1",
                             "1.5+1.git.abc123de" })
    public void testValidLocalVersionIdentifier(String version)
    {
        ArtifactCoordinates coordinates = new MockedMavenArtifactCoordinates();
        coordinates.setVersion(version);
        assertThatCode(
                () -> pypiCanonicalVersionValidator.validate(repository, coordinates)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1!1.0",
                             "1!1.1",
                             "1!2.0",
                             "2!369.89" })
    public void testValidEpochVersion(String version)
    {
        ArtifactCoordinates coordinates = new MockedMavenArtifactCoordinates();
        coordinates.setVersion(version);
        assertThatCode(
                () -> pypiCanonicalVersionValidator.validate(repository, coordinates)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.0.dev456",
                             "1.0a1",
                             "1.0a2.dev456",
                             "1.0a12.dev456",
                             "1.0a12",
                             "1.0b1.dev456",
                             "1.0b2",
                             "1.0b2.post345.dev456",
                             "1.0b2.post345",
                             "1.0rc1.dev456",
                             "1.0rc1",
                             "1.0",
                             "1.0+abc.5",
                             "1.0+abc.7",
                             "1.0+5",
                             "1.0.post456.dev34",
                             "1.0.post456",
                             "1.1.dev1" })
    public void testValidVersionCombinations(String version)
    {
        ArtifactCoordinates coordinates = new MockedMavenArtifactCoordinates();
        coordinates.setVersion(version);
        assertThatCode(
                () -> pypiCanonicalVersionValidator.validate(repository, coordinates)).doesNotThrowAnyException();
    }
}
