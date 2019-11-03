package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.generator.RawArtifactGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Wojciech Pater
 */
public class RawArtifactGeneratorStrategy implements ArtifactGeneratorStrategy<RawArtifactGenerator>
{

    @Override
    public Path generateArtifact(RawArtifactGenerator artifactGenerator,
                                 String id,
                                 String version,
                                 long size,
                                 Map<String, Object> attributesMap)
            throws IOException
    {
        return artifactGenerator.generateArtifact(id, version, size);
    }
}
