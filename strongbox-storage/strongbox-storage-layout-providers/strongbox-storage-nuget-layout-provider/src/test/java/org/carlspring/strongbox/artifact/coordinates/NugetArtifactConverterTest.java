package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.testing.TestCaseWithRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Pablo Tirado
 */
@Execution(CONCURRENT)
public class NugetArtifactConverterTest
        extends TestCaseWithRepository
{

    @Test
    public void testArtifactPathToCoordinatesConversion()
    {
        String path = "Org.Carlspring.Strongbox.Examples.Nuget.Mono/1.0/Org.Carlspring.Strongbox.Examples.Nuget.Mono.1.0.nupkg";
        NugetArtifactCoordinates nac = NugetArtifactCoordinates.parse(path);
        assertEquals("Org.Carlspring.Strongbox.Examples.Nuget.Mono", nac.getId(),
                     "Failed to convert path to artifact coordinates!");
        assertEquals("1.0", nac.getVersion(), "Failed to convert path to artifact coordinates!");

        path = "Org.Carlspring.Strongbox.Examples.Nuget.Mono/1.0/Org.Carlspring.Strongbox.Examples.Nuget.Mono.nuspec";
        nac = NugetArtifactCoordinates.parse(path);
        assertEquals("Org.Carlspring.Strongbox.Examples.Nuget.Mono", nac.getId(),
                     "Failed to convert path to artifact coordinates!");
        assertEquals("1.0", nac.getVersion(), "Failed to convert path to artifact coordinates!");

        path = "Org.Carlspring.Strongbox.Examples.Nuget.Mono/1.0/Org.Carlspring.Strongbox.Examples.Nuget.Mono.1.0.nupkg.sha512";
        nac = NugetArtifactCoordinates.parse(path);
        assertEquals("Org.Carlspring.Strongbox.Examples.Nuget.Mono", nac.getId(),
                     "Failed to convert path to artifact coordinates!");
        assertEquals("1.0", nac.getVersion(), "Failed to convert path to artifact coordinates!");
    }

}
