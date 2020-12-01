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
                                 long bytesSize,
                                 Map<String, Object> attributesMap)
            throws IOException
    {
        NugetArtifactCoordinates coordinates = new NugetArtifactCoordinates(id, version);
        setLicenses(artifactGenerator, attributesMap);

        return artifactGenerator.generateArtifact(coordinates, bytesSize);
    }

}
