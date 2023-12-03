package org.carlspring.strongbox.artifact.coordinates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Pablo Tirado
 */
@Execution(CONCURRENT)
public class NugetArtifactConverterTest
{

    @Test
    public void testArtifactPathToCoordinatesConversion()
    {
        String path = "Org.Carlspring.Strongbox.Examples.Nuget.Mono/1.0/Org.Carlspring.Strongbox.Examples.Nuget.Mono.1.0.nupkg";
        NugetArtifactCoordinates nac = NugetArtifactCoordinates.parse(path);
        assertThat(nac.getId())
                .as("Failed to convert path to artifact coordinates!")
                .isEqualTo("Org.Carlspring.Strongbox.Examples.Nuget.Mono");
        assertThat(nac.getVersion())
                .as("Failed to convert path to artifact coordinates!")
                .isEqualTo("1.0");

        path = "Org.Carlspring.Strongbox.Examples.Nuget.Mono/1.0/Org.Carlspring.Strongbox.Examples.Nuget.Mono.nuspec";
        nac = NugetArtifactCoordinates.parse(path);
        assertThat(nac.getId())
                .as("Failed to convert path to artifact coordinates!")
                .isEqualTo("Org.Carlspring.Strongbox.Examples.Nuget.Mono");
        assertThat(nac.getVersion())
                .as("Failed to convert path to artifact coordinates!")
                .isEqualTo("1.0");

        path = "Org.Carlspring.Strongbox.Examples.Nuget.Mono/1.0/Org.Carlspring.Strongbox.Examples.Nuget.Mono.1.0.nupkg.sha512";
        nac = NugetArtifactCoordinates.parse(path);
        assertThat(nac.getId())
                .as("Failed to convert path to artifact coordinates!")
                .isEqualTo("Org.Carlspring.Strongbox.Examples.Nuget.Mono");
        assertThat(nac.getVersion())
                .as("Failed to convert path to artifact coordinates!")
                .isEqualTo("1.0");
    }

}
