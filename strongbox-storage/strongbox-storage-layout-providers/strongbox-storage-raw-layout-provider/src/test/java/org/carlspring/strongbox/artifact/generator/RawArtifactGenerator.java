package org.carlspring.strongbox.artifact.generator;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

/**
 * @author Wojciech Pater
 */
public class RawArtifactGenerator implements ArtifactGenerator
{

    @Override
    public Path generateArtifact(String id,
                                 String version,
                                 long size)
            throws IOException
    {
        return null;
    }

    @Override
    public Path generateArtifact(URI uri,
                                 long size)
            throws IOException
    {
        return null;
    }
}
