package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.coordinates.RpmArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.RpmArtifactGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class RpmArtifactGeneratorStrategy
        implements ArtifactGeneratorStrategy<RpmArtifactGenerator>
{

    @Override
    public Path generateArtifact(RpmArtifactGenerator artifactGenerator,
                                 String id,
                                 String version,
                                 long byteSize,
                                 Map<String, Object> attributesMap)
        throws IOException
    {
        String release = (String) attributesMap.get("release");
        String packageType = (String) attributesMap.get("packageType");
        String architecture = (String) attributesMap.get("architecture");

        RpmArtifactCoordinates coordinates = new RpmArtifactCoordinates(id,
                                                                        version,
                                                                        release,
                                                                        packageType,
                                                                        architecture);

        artifactGenerator.setCoordinates(coordinates);

        return artifactGenerator.generateArtifact(coordinates.getId(), coordinates.getVersion(), byteSize);
    }
    
}
