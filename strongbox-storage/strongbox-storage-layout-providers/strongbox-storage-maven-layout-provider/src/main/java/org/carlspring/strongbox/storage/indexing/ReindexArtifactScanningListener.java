package org.carlspring.strongbox.storage.indexing;

import java.io.IOException;

import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactScanningListener;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.ScanningResult;
import org.apache.maven.index.context.IndexingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.Arrays.asList;

/**
 * @author carlspring
 */
public class ReindexArtifactScanningListener
        implements ArtifactScanningListener
{

    private static final Logger logger = LoggerFactory.getLogger(ReindexArtifactScanningListener.class);

    private int totalFiles;

    private IndexingContext context;

    private Indexer indexer;


    public ReindexArtifactScanningListener(Indexer indexer)
    {
        this.indexer = indexer;
    }

    @Override
    public void scanningStarted(final IndexingContext context)
    {
        this.context = context;
    }

    @Override
    public void scanningFinished(final IndexingContext context,
                                 final ScanningResult result)
    {
        result.setTotalFiles(totalFiles);

        logger.debug("Scanning finished; total files: {}; has exception: {}",
                     result.getTotalFiles(),
                     result.hasExceptions());
    }

    @Override
    public void artifactError(final ArtifactContext ac,
                              final Exception ex)
    {
        logger.error(ex.getMessage(), ex);
    }

    @Override
    public void artifactDiscovered(final ArtifactContext ac)
    {
        try
        {
            logger.debug("Adding artifact: {}; ctx id: {}; idx dir: {}",
                         new String[]{ ac.getGav()
                                         .getGroupId() + ":" +
                                       ac.getGav()
                                         .getArtifactId() + ":" +
                                       ac.getGav()
                                         .getVersion() + ":" +
                                       ac.getGav()
                                         .getVersion() + ":" +
                                       ac.getGav()
                                         .getClassifier() + ":" +
                                       ac.getGav()
                                         .getExtension(),
                                       context.getId(),
                                       context.getIndexDirectory().toString() });

            getIndexer().addArtifactsToIndex(asList(ac), context);

            totalFiles++;
        }
        catch (IOException ex)
        {
            logger.error("Artifact index error", ex);
        }
    }

    public Indexer getIndexer()
    {
        return indexer;
    }

    public void setIndexer(Indexer indexer)
    {
        this.indexer = indexer;
    }

}
