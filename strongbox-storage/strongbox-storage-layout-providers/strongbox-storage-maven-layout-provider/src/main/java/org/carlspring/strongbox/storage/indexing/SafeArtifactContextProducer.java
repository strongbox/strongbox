package org.carlspring.strongbox.storage.indexing;

import java.io.File;

import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.DefaultArtifactContextProducer;
import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.apache.maven.index.context.IndexingContext;

/**
 * @author Przemyslaw Fusik
 */
public class SafeArtifactContextProducer
        extends DefaultArtifactContextProducer
{

    public SafeArtifactContextProducer(ArtifactPackagingMapper mapper)
    {
        super(mapper);
    }

    @Override
    public ArtifactContext getArtifactContext(IndexingContext context,
                                              File file)
    {
        final ArtifactContext artifactContext = super.getArtifactContext(context, file);
        return artifactContext != null ? new SafeArtifactContext(artifactContext) : artifactContext;
    }
}
