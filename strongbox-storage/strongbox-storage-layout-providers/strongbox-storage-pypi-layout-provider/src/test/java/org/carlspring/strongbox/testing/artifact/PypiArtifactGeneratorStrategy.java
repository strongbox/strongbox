package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.PypiArtifactGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Yuri Zaytsev
 *
 */
public class PypiArtifactGeneratorStrategy
        implements ArtifactGeneratorStrategy<PypiArtifactGenerator>
{
    @Override
    public Path generateArtifact(PypiArtifactGenerator artifactGenerator,
                                 String id,
                                 String version,
                                 long byteSize,
                                 Map<String, Object> attributesMap)
            throws IOException
    {
        PypiArtifactCoordinates coordinates = new PypiArtifactCoordinates(id,
                                                                          version,
                                                                          null,
                                                                          "py3",
                                                                          "none",
                                                                          "any",
                                                                          (String) attributesMap.get("packaging"));
        
        setLicenses(artifactGenerator, attributesMap);
        
        return artifactGenerator.generateArtifact(coordinates, byteSize);
    }
}
