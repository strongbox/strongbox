package org.carlspring.strongbox.artifact;

import java.net.URI;

import org.carlspring.strongbox.storage.ArtifactResolutionException;

public class ArtifactNotFoundException extends ArtifactResolutionException
{

    private URI artifactResource;

    public ArtifactNotFoundException(URI artifactResource)
    {
        super(String.format("Artifact [%s] not found.", artifactResource));
        
        this.artifactResource = artifactResource;
    }

    public URI getArtifactResource()
    {
        return artifactResource;
    }

}
