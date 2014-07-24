package org.carlspring.strongbox.storage.indexing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.index.*;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.expr.SourcedSearchExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.Arrays.asList;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;

public class RepositoryIndexer
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryIndexer.class);

    private static final Version luceneVersion = Version.LUCENE_48;

    private static final String [] luceneFields = new String [] { "g", "a", "v", "p", "c" };

    private static final StandardAnalyzer luceneAnalyzer = new StandardAnalyzer(luceneVersion);

    private Indexer indexer;

    private Scanner scanner;

    private List<IndexCreator> indexers;

    private IndexingContext indexingContext;

    private String repositoryId;

    private File repositoryBasedir;

    private File indexDir;

    private IndexerConfiguration indexerConfiguration;


    public RepositoryIndexer()
    {
    }

    public void close()
            throws IOException
    {
        indexer.closeIndexingContext(indexingContext, false);
    }

    void close(boolean deleteFiles)
            throws IOException
    {
        indexingContext.close(deleteFiles);
    }

    public void delete(final Collection<ArtifactInfo> artifacts)
            throws IOException
    {
        final List<ArtifactContext> delete = new ArrayList<ArtifactContext>();
        for (final ArtifactInfo artifact : artifacts)
        {
            logger.debug("Deleting artifact: {}; ctx id: {}; idx dir: {}",
                         new String[]{ artifact.toString(),
                                       indexingContext.getId(),
                                       indexingContext.getIndexDirectory().toString() });

            delete.add(new ArtifactContext(null, null, null, artifact, null));
        }

        getIndexer().deleteArtifactsFromIndex(delete, indexingContext);
    }

    public Set<ArtifactInfo> search(final String groupId,
                                    final String artifactId,
                                    final String version,
                                    final String packaging,
                                    final String classifier)
            throws IOException
    {
        final BooleanQuery query = new BooleanQuery();

        if (groupId != null)
        {
            query.add(getIndexer().constructQuery(MAVEN.GROUP_ID, new SourcedSearchExpression(groupId)), MUST);
        }

        if (artifactId != null)
        {
            query.add(getIndexer().constructQuery(MAVEN.ARTIFACT_ID, new SourcedSearchExpression(artifactId)), MUST);
        }

        if (version != null)
        {
            query.add(getIndexer().constructQuery(MAVEN.VERSION, new SourcedSearchExpression(version)), MUST);
        }

        if (packaging != null)
        {
            query.add(getIndexer().constructQuery(MAVEN.PACKAGING, new SourcedSearchExpression(packaging)), MUST);
        }
        else
        {
            // Fallback to jar
            query.add(getIndexer().constructQuery(MAVEN.PACKAGING, new SourcedSearchExpression("jar")), MUST);
        }

        if (classifier != null)
        {
            query.add(getIndexer().constructQuery(MAVEN.CLASSIFIER, new SourcedSearchExpression(classifier)), MUST);
        }

        logger.debug("Executing search query: {}; ctx id: {}; idx dir: {}",
                     new String[]{ query.toString(),
                                   indexingContext.getId(),
                                   indexingContext.getIndexDirectory().toString() });

        final FlatSearchResponse response = getIndexer().searchFlat(new FlatSearchRequest(query, indexingContext));

        logger.info("Hit count: {}", response.getReturnedHitsCount());

        final Set<ArtifactInfo> results = response.getResults();
        if (logger.isDebugEnabled())
        {
            for (final ArtifactInfo result : results)
            {
                logger.debug("Found artifact: {}", result.toString());
            }
        }

        return results;
    }

    public Set<ArtifactInfo> search(final String queryText)
            throws ParseException, IOException
    {
        final Query query = new MultiFieldQueryParser(luceneVersion, luceneFields, luceneAnalyzer).parse(queryText);

        logger.debug("Executing search query: {}; ctx id: {}; idx dir: {}",
                     new String[]{ query.toString(),
                                   indexingContext.getId(),
                                   indexingContext.getIndexDirectory().toString() });

        final FlatSearchResponse response = getIndexer().searchFlat(new FlatSearchRequest(query, indexingContext));

        logger.info("Hit count: {}", response.getReturnedHitsCount());

        final Set<ArtifactInfo> results = response.getResults();
        if (logger.isDebugEnabled())
        {
            for (final ArtifactInfo result : results)
            {
                logger.debug("Found artifact: {}; uinfo: {}", result.toString(), result.getUinfo());
            }
        }

        return results;
    }

    public Set<ArtifactInfo> searchBySHA1(final String checksum)
            throws IOException
    {
        final BooleanQuery query = new BooleanQuery();
        query.add(getIndexer().constructQuery(MAVEN.SHA1, new SourcedSearchExpression(checksum)), MUST);

        logger.debug("Executing search query: {}; ctx id: {}; idx dir: {}",
                     new String[]{ query.toString(),
                                   indexingContext.getId(),
                                   indexingContext.getIndexDirectory().toString() });

        final FlatSearchResponse response = getIndexer().searchFlat(new FlatSearchRequest(query, indexingContext));
        logger.info("Hit count: {}", response.getReturnedHitsCount());

        final Set<ArtifactInfo> results = response.getResults();
        if (logger.isDebugEnabled())
        {
            for (final ArtifactInfo result : results)
            {
                logger.debug("Found artifact: {}", result.toString());
            }
        }

        return results;
    }

    public int index(final File startingPath)
    {
        final ScanningResult scan = getScanner().scan(new ScanningRequest(indexingContext,
                                                                          new ReindexArtifactScanningListener(),
                                                                          startingPath == null ? "." :
                                                                          startingPath.getPath()));
        return scan.getTotalFiles();
    }

    public void addArtifactToIndex(final File artifactFile, final ArtifactInfo artifactInfo) throws IOException
    {
        getIndexer().addArtifactsToIndex(asList(new ArtifactContext(null, artifactFile, null, artifactInfo, null)),
                                         indexingContext);
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
        if (artifact.getType() != null)
        {
            artifactInfo.setFieldValue(MAVEN.PACKAGING, artifact.getType());
        }

        logger.info("adding artifact: {}; repo: {}; type: {}", new String[] { artifactInfo.getUinfo(),
                                                                              repository, artifact.getType() });

        getIndexer().addArtifactsToIndex(asList(new ArtifactContext(null,
                                                                    artifactFile,
                                                                    null,
                                                                    artifactInfo,
                                                                    artifactInfo.calculateGav())), indexingContext);
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
                logger.debug("Adding artifact gav: {}; ctx id: {}; idx dir: {}",
                             new String[]{ ac.getGav().toString(),
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
    }

    public IndexerConfiguration getIndexerConfiguration()
    {
        return indexerConfiguration;
    }

    public void setIndexerConfiguration(IndexerConfiguration indexerConfiguration)
    {
        this.indexerConfiguration = indexerConfiguration;
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

    public IndexingContext getIndexingContext()
    {
        return indexingContext;
    }

    public void setIndexingContext(IndexingContext indexingContext)
    {
        this.indexingContext = indexingContext;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public File getRepositoryBasedir()
    {
        return repositoryBasedir;
    }

    public void setRepositoryBasedir(File repositoryBasedir)
    {
        this.repositoryBasedir = repositoryBasedir;
    }

    public File getIndexDir()
    {
        return indexDir;
    }

    public void setIndexDir(File indexDir)
    {
        this.indexDir = indexDir;
    }

}
