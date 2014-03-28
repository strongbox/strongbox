package org.carlspring.strongbox.storage.indexing;

import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.maven.index.*;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Indexing // extends PlexusTestCase
{
    public void index2() throws PlexusContainerException, ComponentLookupException, IOException
    {
        final PlexusContainer plexusContainer = new DefaultPlexusContainer();
        File centralLocalCache = new File( "target" );
        File centralIndexDir = new File( "target/central-index" );

        // Creators we want to use (search for fields it defines)
        List<IndexCreator> indexers = new ArrayList<IndexCreator>();
        indexers.add( plexusContainer.lookup( IndexCreator.class, "min" ) );
        indexers.add( plexusContainer.lookup( IndexCreator.class, "jarContent" ) );
        indexers.add( plexusContainer.lookup( IndexCreator.class, "maven-plugin" ) );

        final Indexer indexer = plexusContainer.lookup(Indexer.class);

        plexusContainer.lookup(LoggerManager.class).setThresholds( Logger.LEVEL_DEBUG );

        // Create context for central repository index
        final IndexingContext centralContext =
                indexer.createIndexingContext("central-context", "central", centralLocalCache, centralIndexDir,
                        null, null, true, true, indexers);

        final ScanningRequest scanningRequest = new ScanningRequest(centralContext, new ArtifactScanningListener()
        {
            @Override
            public void scanningStarted(IndexingContext ctx)
            {
                System.out.println("started");
            }

            @Override
            public void scanningFinished(IndexingContext ctx, ScanningResult result)
            {
                System.out.println("finished");
            }

            @Override
            public void artifactError(ArtifactContext ac, Exception e)
            {
                System.out.println(e);
            }

            @Override
            public void artifactDiscovered(ArtifactContext ac)
            {
                try
                {
                    indexer.addArtifactsToIndex(Arrays.asList(ac), centralContext);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                System.out.println(ac.getArtifact());
            }
        }, "x");
        Scanner scanner = plexusContainer.lookup(Scanner.class);
        final ScanningResult scan = scanner.scan(scanningRequest);
        System.out.println(scan.getTotalFiles());

        FlatSearchResponse response = indexer.searchFlat( new FlatSearchRequest( new MatchAllDocsQuery(), centralContext ) );
        System.out.println(response.getResults().size());

        for ( ArtifactInfo ai : response.getResults() )
        {
            System.out.println( ai.toString() );
        }
    }

    public static void main(String[] args) throws Exception
    {
        new Indexing().index2();
    }
}
