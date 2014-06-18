package org.carlspring.strongbox.storage.indexing;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.index.*;
import org.apache.maven.index.Scanner;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.expr.SourcedSearchExpression;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;
import static org.apache.lucene.search.BooleanClause.Occur.MUST_NOT;

public class RepositoryIndexer
{

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RepositoryIndexer.class);
    private static final Version luceneVersion = Version.LUCENE_36;
    private static final String [] luceneFields = new String [] {
            "g", "a", "v", "p", "c"
    };
    private static final StandardAnalyzer luceneAnalyzer = new StandardAnalyzer(luceneVersion);

    private PlexusContainer plexus;

    private Indexer indexer;

    private Scanner scanner;

    private List<IndexCreator> indexers;

    private IndexingContext context;


    public RepositoryIndexer(final String repositoryId,
                             final File repositoryBasedir,
                             final File indexDir)
            throws PlexusContainerException,
                   ComponentLookupException,
                   IOException
    {
        plexus = new DefaultPlexusContainer();
        indexer = plexus.lookup(Indexer.class);
        scanner = plexus.lookup(Scanner.class);

        // @TODO: make a list of indexers configurable
        indexers = asList(plexus.lookup(IndexCreator.class, "min"),
                          plexus.lookup(IndexCreator.class, "jarContent"),
                          plexus.lookup(IndexCreator.class, "maven-plugin"));

        // @TODO: remove once no longer needed
        plexus.lookup(LoggerManager.class).setThresholds(Logger.LEVEL_DEBUG);

        context = indexer.createIndexingContext(repositoryId + "/ctx",
                                                repositoryId,
                                                repositoryBasedir,
                                                indexDir,
                                                null,
                                                null,
                                                true, // if context should be searched in non-targeted mode.
                                                true, // if indexDirectory is known to contain (or should contain)
                                                      // valid Maven Indexer lucene index, and no checks needed to be
                                                      // performed, or, if we want to "stomp" over existing index
                                                      // (unsafe to do!).
                                                indexers);
        logger.info("repository indexer created; id: {}; dir: {}", repositoryId, indexDir);
    }

    void close(boolean deleteFiles)
            throws IOException
    {
        context.close(deleteFiles);
    }

    public void delete(final Collection<ArtifactInfo> artifacts)
            throws IOException
    {
        final List<ArtifactContext> delete = new ArrayList<ArtifactContext>();
        for (final ArtifactInfo artifact : artifacts)
        {
            logger.info("deleting artifact: {}; ctx id: {}; idx dir: {}",
                    new String [] { artifact.toString(), context.getId(), context.getIndexDirectory().toString() });
            delete.add(new ArtifactContext(null, null, null, artifact, null));
        }

        indexer.deleteArtifactsFromIndex(delete, context);
    }

    public Set<ArtifactInfo> search(final String groupId,
                                    final String artifactId,
                                    final String version)
            throws IOException
    {
        final BooleanQuery query = new BooleanQuery();
        query.add(indexer.constructQuery(MAVEN.GROUP_ID, new SourcedSearchExpression(groupId)), MUST);
        query.add(indexer.constructQuery(MAVEN.ARTIFACT_ID, new SourcedSearchExpression(artifactId)), MUST);
        query.add(indexer.constructQuery(MAVEN.PACKAGING, new SourcedSearchExpression("jar")), MUST);
        query.add(indexer.constructQuery(MAVEN.CLASSIFIER, new SourcedSearchExpression(Field.NOT_PRESENT)), MUST_NOT);

        if (version != null)
        {
            query.add(indexer.constructQuery(MAVEN.VERSION, new SourcedSearchExpression(version)), MUST);
        }

        logger.info("running search query: {}; ctx id: {}; idx dir: {}",
                new String [] { query.toString(), context.getId(), context.getIndexDirectory().toString() });
        final FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(query, context));
        logger.info("hit count: {}", response.getReturnedHitsCount());
        final Set<ArtifactInfo> results = response.getResults();
        if (logger.isDebugEnabled())
        {
            for (final ArtifactInfo result : results)
            {
                logger.debug("found artifact: {}", result.toString());
            }
        }
        return results;
    }

    public Set<ArtifactInfo> search(final String queryText)
            throws org.apache.lucene.queryParser.ParseException, IOException
    {
        final Query query = new MultiFieldQueryParser(luceneVersion, luceneFields, luceneAnalyzer).parse(queryText);
        logger.info("running search query: {}; ctx id: {}; idx dir: {}",
                new String [] { query.toString(), context.getId(), context.getIndexDirectory().toString() });
        final FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(query, context));
        logger.info("hit count: {}", response.getReturnedHitsCount());
        final Set<ArtifactInfo> results = response.getResults();
        if (logger.isDebugEnabled())
        {
            for (final ArtifactInfo result : results)
            {
                logger.debug("found artifact: {}", result.toString());
            }
        }
        return results;
    }

    public int index(final File startingPath)
    {
        final ScanningResult scan = scanner.scan(new ScanningRequest(context,
                                                                     new ReindexArtifactScanningListener(),
                                                                     startingPath == null ? "." :
                                                                     startingPath.getPath()));
        return scan.getTotalFiles();
    }

    public void addArtifactToIndex(final File artifactFile, final ArtifactInfo artifactInfo) throws IOException
    {
        indexer.addArtifactsToIndex(asList(new ArtifactContext(null, artifactFile, null, artifactInfo, null)), context);
    }

    public void addArtifactToIndex(String repository,
                                   final File artifactFile,
                                   final Artifact artifact)
            throws IOException
    {
        ArtifactInfo artifactInfo = new ArtifactInfo(repository,
                                                     artifact.getGroupId(),
                                                     artifact.getArtifactId(),
                                                     artifact.getVersion(),
                                                     artifact.getClassifier());
        indexer.addArtifactsToIndex(asList(new ArtifactContext(null, artifactFile, null, artifactInfo, null)), context);
    }

    private class ReindexArtifactScanningListener
            implements ArtifactScanningListener
    {

        int totalFiles = 0;
        private IndexingContext context;

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
            logger.error("artifact error", ex);
        }

        @Override
        public void artifactDiscovered(final ArtifactContext ac)
        {
            try
            {
                logger.info("adding artifact gav: {}; ctx id: {}; idx dir: {}",
                        new String [] { ac.getGav().toString(), context.getId(), context.getIndexDirectory().toString() });
                indexer.addArtifactsToIndex(asList(ac), context);
                totalFiles++;
            }
            catch (IOException ex)
            {
                logger.error("artifact index error", ex);
            }
        }
    }

    public PlexusContainer getPlexus()
    {
        return plexus;
    }

    public void setPlexus(PlexusContainer plexus)
    {
        this.plexus = plexus;
    }

    public Indexer getIndexer()
    {
        return indexer;
    }

    public void setIndexer(Indexer indexer)
    {
        this.indexer = indexer;
    }

    public Scanner getScanner()
    {
        return scanner;
    }

    public void setScanner(Scanner scanner)
    {
        this.scanner = scanner;
    }

    public List<IndexCreator> getIndexers()
    {
        return indexers;
    }

    public void setIndexers(List<IndexCreator> indexers)
    {
        this.indexers = indexers;
    }

    public IndexingContext getContext()
    {
        return context;
    }

    public void setContext(IndexingContext context)
    {
        this.context = context;
    }

}
