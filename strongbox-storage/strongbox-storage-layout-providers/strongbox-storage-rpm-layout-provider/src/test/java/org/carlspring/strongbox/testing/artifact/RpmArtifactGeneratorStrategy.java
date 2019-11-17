package org.carlspring.strongbox.testing.artifact;

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
                                 long size,
                                 Map<String, Object> attributesMap)
        throws IOException
    {
//        RpmArtifactCoordinates coordinates = new RpmArtifactCoordinates((String) attributesMap.get("scope"),
//                                                                        id,
//                                                                        version,
//                                                                        (String) attributesMap.get("extension"));
//        Path packagePath = artifactGenerator.generateArtifact(coordinates, bytesSize);
//        if (!Optional.ofNullable(attributesMap.get("repositoryId"))
//                     .filter(repositoryId -> ((String) repositoryId).trim().length() > 0)
//                     .isPresent())
//        {
//            // if package won't be deployed then `publish.json` will be generated
//            artifactGenerator.buildPublishJson(bytesSize);
//        }
//
//        return packagePath;

        return null;
    }
    
}
