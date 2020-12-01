package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author sbespalov
 *
 * @param <T>
 */
public interface ArtifactGeneratorStrategy<T extends ArtifactGenerator>
{

    Path generateArtifact(T artifactGenerator,
                          String id,
                          String version,
                          long size,
                          Map<String, Object> attributesMap)
        throws IOException;
    
    default void setLicenses(ArtifactGenerator artifactGenerator,
                             Map<String, Object> attributesMap) 
        throws IOException
    {
        Object licenses = attributesMap.get("licenses");
        if (licenses instanceof LicenseConfiguration[])
        {
            artifactGenerator.setLicenses((LicenseConfiguration[]) licenses);
        }
    }

}
