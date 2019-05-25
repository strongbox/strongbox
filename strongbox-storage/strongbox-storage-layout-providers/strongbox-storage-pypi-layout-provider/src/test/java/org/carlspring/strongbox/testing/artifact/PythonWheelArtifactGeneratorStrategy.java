package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.generator.PythonWheelArtifactGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Yuri Zaytsev
 *
 */
public class PythonWheelArtifactGeneratorStrategy implements ArtifactGeneratorStrategy<PythonWheelArtifactGenerator>
{
    @Override
    public Path generateArtifact(PythonWheelArtifactGenerator artifactGenerator,
                                 String id,
                                 String version,
                                 int size,
                                 Map<String, Object> attributesMap)
            throws IOException
    {
        return artifactGenerator.generateArtifact(id, version, size);
    }
}
