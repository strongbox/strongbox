package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.NpmArtifactGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

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
                                 long bytesSize,
                                 Map<String, Object> attributesMap)
            throws IOException
    {
        NpmArtifactCoordinates coordinates = new NpmArtifactCoordinates((String) attributesMap.get("scope"),
                                                                        id,
                                                                        version,
                                                                        (String) attributesMap.get("extension"));
        
        setLicenses(artifactGenerator, attributesMap);

        Path packagePath = artifactGenerator.generateArtifact(coordinates, bytesSize);
        if (!Optional.ofNullable(attributesMap.get("repositoryId"))
                     .filter(repositoryId -> ((String) repositoryId).trim().length() > 0)
                     .isPresent())
        {
            // if package won't be deployed then `publish.json` will be generated
            artifactGenerator.buildPublishJson(bytesSize);
        }
        
        return packagePath;
    }
}
