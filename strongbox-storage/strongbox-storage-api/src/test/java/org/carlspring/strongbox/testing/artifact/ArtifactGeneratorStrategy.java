package org.carlspring.strongbox.testing.artifact;

import java.io.IOException;
import java.nio.file.Path;

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
                          int size) throws IOException;

}
