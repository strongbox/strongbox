package org.carlspring.strongbox.artifact.generator;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

/**
 * @author sbespalov
 *
 */
public interface ArtifactGenerator
{

    Path generateArtifact(String id,
                          String version,
                          int size)
        throws IOException;

    
    Path generateArtifact(URI uri,
                          int size)
        throws IOException;

}
