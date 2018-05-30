package org.carlspring.strongbox.artifact;

import java.io.IOException;
import java.net.URI;

public class ArtifactNotFoundException extends IOException
{

    private URI artifactResource;

    public ArtifactNotFoundException(URI artifactResource)
    {
        super();
        this.artifactResource = artifactResource;
    }

    public URI getArtifactResource()
    {
        return artifactResource;
    }

}
