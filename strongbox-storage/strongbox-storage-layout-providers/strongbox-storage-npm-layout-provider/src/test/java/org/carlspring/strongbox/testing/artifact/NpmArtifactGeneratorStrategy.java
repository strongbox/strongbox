package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.NpmArtifactGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Yuri Zaytsev
 *
 */
public class NpmArtifactGeneratorStrategy implements ArtifactGeneratorStrategy<NpmArtifactGenerator>
{

    @Override
    public Path generateArtifact(NpmArtifactGenerator artifactGenerator,
                                 String id,
                                 String version,
                                 int size,
                                 Map<String, Object> attributesMap)
            throws IOException
    {
        NpmArtifactCoordinates coordinates = new NpmArtifactCoordinates((String) attributesMap.get("scope"),
                                                                        id,
                                                                        version,
                                                                        (String) attributesMap.get("extension"));
        return artifactGenerator.generateArtifact(coordinates);
    }
}
