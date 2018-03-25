package org.carlspring.strongbox.artifact;

import java.io.File;
import java.nio.file.Path;

import org.apache.maven.artifact.Artifact;

/**
 * @author Przemyslaw Fusik
 */
public interface MavenArtifact
        extends Artifact
{

    @Deprecated
    @Override
    default File getFile()
    {
        throw new UnsupportedOperationException("Use getPath instead");
    }

    @Deprecated
    @Override
    default void setFile(File destination)
    {
        throw new UnsupportedOperationException("Use setPath instead");
    }

    Path getPath();

    void setPath(Path destination);
}
