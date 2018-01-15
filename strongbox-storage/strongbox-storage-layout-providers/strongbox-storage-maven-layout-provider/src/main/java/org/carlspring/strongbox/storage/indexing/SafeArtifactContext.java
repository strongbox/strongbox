package org.carlspring.strongbox.storage.indexing;

import org.apache.maven.index.ArtifactContext;

/**
 * @author Przemyslaw Fusik
 */
public class SafeArtifactContext
        extends ArtifactContext
{

    public SafeArtifactContext(ArtifactContext ac)
    {
        super(ac.getPom(), ac.getArtifact(), ac.getMetadata(), ac.getArtifactInfo(), ac.getGav());
    }
}
