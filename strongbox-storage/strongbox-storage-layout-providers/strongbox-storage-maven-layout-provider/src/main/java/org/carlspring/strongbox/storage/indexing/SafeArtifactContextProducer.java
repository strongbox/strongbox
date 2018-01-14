package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.providers.io.RepositoryPath;

import java.io.File;

import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.DefaultArtifactContextProducer;
import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.context.IndexingContext;

/**
 * @author Przemyslaw Fusik
 */
public class SafeArtifactContextProducer
        extends DefaultArtifactContextProducer
{

    private final RepositoryPath artifactPath;

    public SafeArtifactContextProducer(final ArtifactPackagingMapper mapper,
                                       final RepositoryPath artifactPath)
    {
        super(mapper);
        this.artifactPath = artifactPath;
    }

    @Override
    public ArtifactContext getArtifactContext(IndexingContext context,
                                              File file)
    {
        final ArtifactContext artifactContext = super.getArtifactContext(context, file);
        return artifactContext != null ? new SafeArtifactContext(artifactContext) : artifactContext;
    }

    @Override
    protected Gav getGavFromPath(final IndexingContext context,
                                 final String repositoryPath,
                                 final String artifactPath)
    {
        return context.getGavCalculator().pathToGav(this.artifactPath.relativize().toString().replace('\\', '/'));
    }
}
