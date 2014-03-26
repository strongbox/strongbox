package org.carlspring.strongbox.storage.indexing;

import org.apache.maven.index.Indexer;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.packer.IndexPacker;
import org.apache.maven.index.packer.IndexPackingRequest;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Indexing
{
    public static void index() throws PlexusContainerException, ComponentLookupException, IOException
    {
        final PlexusContainer plexusContainer = new DefaultPlexusContainer();
        final Indexer indexer = plexusContainer.lookup(Indexer.class);
        final IndexPacker indexPacker = plexusContainer.lookup(IndexPacker.class);

        final File centralIndexDir = new File("target/central-index");
        centralIndexDir.mkdirs();
        final File indexDir = new File("target/test-index");
        indexDir.mkdirs();

        final List<IndexCreator> indexers = new ArrayList<IndexCreator>();
        indexers.add(plexusContainer.lookup(IndexCreator.class, "min"));
        indexers.add(plexusContainer.lookup(IndexCreator.class, "jarContent"));

        final File repositoryDir = new File("/Users/denis/.m2/repository");
        final IndexingContext indexingContext =
                indexer.createIndexingContext("central-context", "central", repositoryDir, centralIndexDir, null, null,
                        true, true, indexers);
        final IndexPackingRequest packingRequest = new IndexPackingRequest(indexingContext, indexDir);
        indexPacker.packIndex(packingRequest);
    }

    public static void main(String[] args) throws PlexusContainerException, ComponentLookupException, IOException
    {
        index();
    }
}
