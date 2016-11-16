package org.carlspring.strongbox.artifact.coordinates;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NugetPackageConverterTest
{

    @Test
    public void testArtifactPathToCoordinatesConversion()
            throws Exception
    {
        String path = "Org.Carlspring.Strongbox.Examples.Nuget.Mono/1.0/Org.Carlspring.Strongbox.Examples.Nuget.Mono.1.0.nupkg";
        
        NugetHierarchicalArtifactCoordinates nac = new NugetHierarchicalArtifactCoordinates(path);

        assertEquals("Failed to convert path to artifact coordinates!", "Org.Carlspring.Strongbox.Examples.Nuget.Mono", nac.getId());
        assertEquals("Failed to convert path to artifact coordinates!", "1.0", nac.getVersion());
    }

    
}
