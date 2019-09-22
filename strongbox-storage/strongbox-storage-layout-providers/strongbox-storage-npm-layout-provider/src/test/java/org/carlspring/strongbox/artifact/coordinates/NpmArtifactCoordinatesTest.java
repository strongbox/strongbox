package org.carlspring.strongbox.artifact.coordinates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author sbespalov
 * @author Pablo Tirado
 */
@Execution(CONCURRENT)
public class NpmArtifactCoordinatesTest
{
    @Test
    public void testArtifactPathToCoordinatesConversion()
    {
        NpmArtifactCoordinates coordinates = NpmArtifactCoordinates.parse(
                "react-redux/react-redux/5.0.6/react-redux-5.0.6.tgz");

        assertThat(coordinates.getScope()).isNull();
        assertThat(coordinates.getName()).isEqualTo("react-redux");
        assertThat(coordinates.getVersion()).isEqualTo("5.0.6");
        assertThat(coordinates.getExtension()).isEqualTo("tgz");

        coordinates = NpmArtifactCoordinates.parse("@types/node/8.0.51/node-8.0.51.tgz");

        assertThat(coordinates.getScope()).isEqualTo("@types");
        assertThat(coordinates.getName()).isEqualTo("node");
        assertThat(coordinates.getVersion()).isEqualTo("8.0.51");
        assertThat(coordinates.getExtension()).isEqualTo("tgz");
    }

    @Test
    public void testMetadataPathToCoordinatesConversion()
    {
        NpmArtifactCoordinates coordinates = NpmArtifactCoordinates.parse("react-redux/react-redux/5.0.6/package.json");

        assertThat(coordinates.getScope()).isNull();
        assertThat(coordinates.getName()).isEqualTo("react-redux");
        assertThat(coordinates.getVersion()).isEqualTo("5.0.6");
        assertThat(coordinates.getExtension()).isEqualTo("json");

        coordinates = NpmArtifactCoordinates.parse("@types/node/8.0.51/package.json");

        assertThat(coordinates.getScope()).isEqualTo("@types");
        assertThat(coordinates.getName()).isEqualTo("node");
        assertThat(coordinates.getVersion()).isEqualTo("8.0.51");
        assertThat(coordinates.getExtension()).isEqualTo("json");
    }

    @Test
    void testVersionAssertion()
    {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> NpmArtifactCoordinates.parse("@types/node/8.beta1/node-8.beta1.tgz"));
    }

    @Test
    void testNameAssertion()
    {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> NpmArtifactCoordinates.parse("@types/_node/8.0.51/node-8.0.51.tgz"));
    }
}
