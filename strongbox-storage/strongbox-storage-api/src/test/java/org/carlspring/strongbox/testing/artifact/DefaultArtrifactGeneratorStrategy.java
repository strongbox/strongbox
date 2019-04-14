package org.carlspring.strongbox.testing.artifact;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;

public class DefaultArtrifactGeneratorStrategy implements ArtifactGeneratorStrategy<ArtifactGenerator>
{

    @Override
    public Path generateArtifact(ArtifactGenerator artifactGenerator,
                                 String id,
                                 String version,
                                 int size,
                                 Map<String, Object> attributesMap) throws IOException
    {
        return artifactGenerator.generateArtifact(id, version, size);
    }
    
}
