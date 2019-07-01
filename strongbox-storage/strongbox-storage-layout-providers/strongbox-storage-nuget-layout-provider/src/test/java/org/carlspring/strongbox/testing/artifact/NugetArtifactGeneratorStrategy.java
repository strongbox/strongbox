package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.NugetArtifactGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class NugetArtifactGeneratorStrategy
        implements ArtifactGeneratorStrategy<NugetArtifactGenerator>
{

    @Override
    public Path generateArtifact(NugetArtifactGenerator artifactGenerator,
                                 String id,
                                 String version,
                                 int size,
                                 Map<String, Object> attributesMap)
            throws IOException
    {
        String packaging = (String) attributesMap.get("packaging");
        NugetArtifactCoordinates coordinates = new NugetArtifactCoordinates(id, version, packaging);

        return artifactGenerator.generateArtifact(coordinates);
    }

}
