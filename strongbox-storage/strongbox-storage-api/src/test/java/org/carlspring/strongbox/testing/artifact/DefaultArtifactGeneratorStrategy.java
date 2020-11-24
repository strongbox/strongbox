package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class DefaultArtifactGeneratorStrategy
        implements ArtifactGeneratorStrategy<ArtifactGenerator>
{

    @Override
    public Path generateArtifact(ArtifactGenerator artifactGenerator,
                                 String id,
                                 String version,
                                 long size,
                                 Map<String, Object> attributesMap)
            throws IOException
    {
        setLicenses(artifactGenerator, attributesMap);

        return artifactGenerator.generateArtifact(id, version, size);
    }

}
