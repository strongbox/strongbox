package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.DefaultArtifactContextProducer;
import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.context.IndexingContext;

/**
 * @author Przemyslaw Fusik
 */
@Deprecated
public class SafeArtifactContextProducer
        extends DefaultArtifactContextProducer
{

    private static final Logger logger = LoggerFactory.getLogger(SafeArtifactContextProducer.class);
    
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
        try
        {
            return context.getGavCalculator().pathToGav(RepositoryFiles.relativizePath(this.artifactPath));
        }
        catch (IOException e)
        {
            logger.error(String.format("Failed to resolve artifact path [%s]", this.artifactPath), e);
            return null;
        }
    }
}
