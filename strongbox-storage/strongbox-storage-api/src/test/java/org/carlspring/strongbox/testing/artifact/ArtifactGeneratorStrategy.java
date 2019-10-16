package org.carlspring.strongbox.testing.artifact;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;

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

}
