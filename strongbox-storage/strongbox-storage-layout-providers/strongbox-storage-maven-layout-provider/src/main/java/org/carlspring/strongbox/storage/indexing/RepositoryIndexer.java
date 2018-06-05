package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenRepositoryArtifact;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.search.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactContextProducer;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.FlatSearchRequest;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.Scanner;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.expr.SourcedSearchExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;

public class RepositoryIndexer
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryIndexer.class);

    private static final String[] luceneFields = new String[]{ "g",
                                                               "a",
                                                               "v",
                                                               "p",
                                                               "l" };

    private static final WhitespaceAnalyzer luceneAnalyzer = new WhitespaceAnalyzer();

    private Indexer indexer;

    private Scanner scanner;

    private List<IndexCreator> indexers;

    private IndexingContext indexingContext;

    private String storageId;

    private String repositoryId;

    private RepositoryPath indexDir;

    private IndexerConfiguration indexerConfiguration;

    private Configuration configuration;

    private String contextId;

    private ApplicationContext applicationContext;


    public RepositoryIndexer(String contextId)
    {
        this.contextId = contextId;
    }

    public void addArtifactToIndex(final RepositoryPath artifactPath)
            throws IOException
    {
        try
        {
            final ArtifactContextProducer artifactContextProducer = applicationContext.getBean(
                    ArtifactContextProducer.class, artifactPath);
            ArtifactContext artifactContext = artifactContextProducer.getArtifactContext(indexingContext,
                                                                                         artifactPath.toAbsolutePath().toFile());

            if (artifactContext == null)
            {
                return;
            }
            final ArtifactInfo artifactInfo = artifactContext.getArtifactInfo();

            // preserve duplicates
            if (CollectionUtils.isNotEmpty(
                    search(artifactInfo)))
            {
                return;
            }
            getIndexer().addArtifactToIndex(artifactContext, indexingContext);
        }
        catch (Exception e) // it's not really a critical problem, artifacts could be added to index later
        {
            logger.warn("Unable to add artifacts to index", e);
        }
    }

    public void delete(final Collection<ArtifactInfo> artifactInfos)
            throws IOException
    {
        final List<ArtifactContext> delete = new ArrayList<>();
        for (final ArtifactInfo artifactInfo : artifactInfos)
        {
            // preserve extra delete index records
            if (CollectionUtils.isEmpty(search(artifactInfo)))
            {
                continue;
            }
            delete.add(new SafeArtifactContext(new ArtifactContext(null, null, null, artifactInfo, null)));
        }

        getIndexer().deleteArtifactsFromIndex(delete, indexingContext);
    }

    public Set<SearchResult> search(final ArtifactInfo artifactInfo)
        throws IOException
    {
        return search(artifactInfo.getGroupId(), artifactInfo.getArtifactId(),
                                           artifactInfo.getVersion(),
                                           artifactInfo.getFileExtension(), artifactInfo.getClassifier());
    }

    public Set<SearchResult> search(final String groupId,
                                    final String artifactId,
                                    final String version,
                                    final String extension,
                                    final String classifier)
            throws IOException
    {
        final Builder booleanQueryBuiler = new Builder();

        if (groupId != null)
        {
            booleanQueryBuiler.add(getIndexer().constructQuery(MAVEN.GROUP_ID, new SourcedSearchExpression(groupId)), MUST);
        }

        if (artifactId != null)
        {
        	booleanQueryBuiler.add(getIndexer().constructQuery(MAVEN.ARTIFACT_ID, new SourcedSearchExpression(artifactId)), MUST);
        }

        if (version != null)
        {
        	booleanQueryBuiler.add(getIndexer().constructQuery(MAVEN.VERSION, new SourcedSearchExpression(version)), MUST);
        }

        if (extension != null)
        {
        	booleanQueryBuiler.add(getIndexer().constructQuery(MAVEN.EXTENSION, new SourcedSearchExpression(extension)), MUST);
        }
        else
        {
            // Fallback to jar
        	booleanQueryBuiler.add(getIndexer().constructQuery(MAVEN.PACKAGING, new SourcedSearchExpression("jar")), MUST);
        }

        if (classifier != null)
        {
        	booleanQueryBuiler.add(getIndexer().constructQuery(MAVEN.CLASSIFIER, new SourcedSearchExpression(classifier)), MUST);
        }

        final BooleanQuery booleanQuery = booleanQueryBuiler.build();

        logger.debug("Executing search query: {}; ctx id: {}; idx dir: {}",
                     new String[]{ booleanQuery.toString(),
                                   indexingContext.getId(),
                                   indexingContext.getIndexDirectory().toString() });



        final FlatSearchResponse response = getIndexer().searchFlat(new FlatSearchRequest(booleanQuery, indexingContext));

        logger.debug("Hit count: {}", response.getReturnedHitsCount());

        final Set<ArtifactInfo> r = response.getResults();
        final Set<SearchResult> results = asSearchResults(r);

        if (logger.isDebugEnabled())
        {
            for (final SearchResult result : results)
            {
                logger.debug("Found artifact: {}", result.toString());
            }
        }

        return results;
    }

    public Set<SearchResult> search(final String queryText)
            throws ParseException, IOException
    {
        try
        {
            final Query query = new MultiFieldQueryParser(luceneFields, luceneAnalyzer).parse(queryText);

            logger.debug("Text of the query: {}", queryText);
            logger.debug("Executing search query: {}; ctx id: {}; idx dir: {}",
                         new String[]{ query.toString(),
                                       indexingContext.getId(),
                                       indexingContext.getIndexDirectory().toString() });

            FlatSearchRequest searchRequest = new FlatSearchRequest(query,
                                                                    Comparator.comparing(this::calculateArtifactInfo),
                                                                    indexingContext);

            try (final FlatSearchResponse response = getIndexer().searchFlat(searchRequest))
            {

                logger.debug("Hit count: {}", response.getReturnedHitsCount());

                final Set<ArtifactInfo> r = response.getResults();
                final Set<SearchResult> results = asSearchResults(r);

                if (logger.isDebugEnabled())
                {
                    for (final SearchResult result : results)
                    {
                        logger.debug("Found artifact: {}", result.toString());
                    }
                }
                return results;
            }
        }
        catch (Exception e)
        {
            logger.warn("Unable execute search query", e);

            return new HashSet<>();
        }
    }

    protected String calculateArtifactInfo(ArtifactInfo a1)
    {
        return a1.toString();
    }

    public Set<SearchResult> searchBySHA1(final String checksum)
            throws IOException
    {
        final Builder booleanQueryBuilder = new Builder();

        booleanQueryBuilder.add(getIndexer().constructQuery(MAVEN.SHA1, new SourcedSearchExpression(checksum)), MUST);

        final BooleanQuery booleanQuery = booleanQueryBuilder.build();

        logger.debug("Executing search query: {}; ctx id: {}; idx dir: {}",
                     new String[]{ booleanQuery.toString(),
                                   indexingContext.getId(),
                                   indexingContext.getIndexDirectory().toString() });

        final FlatSearchResponse response = getIndexer().searchFlat(new FlatSearchRequest(booleanQuery, indexingContext));

        logger.debug("Hit count: {}", response.getReturnedHitsCount());

        final Set<ArtifactInfo> r = response.getResults();
        final Set<SearchResult> results = asSearchResults(r);

        if (logger.isDebugEnabled())
        {
            for (final SearchResult result : results)
            {
                logger.debug("Found artifact: {}", result.toString());
            }
        }

        return results;
    }

    private Set<SearchResult> asSearchResults(Set<ArtifactInfo> artifactInfos)
    {
        Set<SearchResult> results = new LinkedHashSet<>(artifactInfos.size());
        for (ArtifactInfo artifactInfo : artifactInfos)
        {
            MavenArtifact artifact = new MavenRepositoryArtifact(artifactInfo.getGroupId(),
                                                               artifactInfo.getArtifactId(),
                                                               artifactInfo.getVersion(),
                                                               artifactInfo.getFileExtension(),
                                                               artifactInfo.getClassifier());

            MavenArtifactCoordinates artifactCoordinates = new MavenArtifactCoordinates(artifact);
            String url = getURLForArtifact(storageId, repositoryId, artifactCoordinates.toPath());
            final SearchResult result = new SearchResult(storageId,
                                                         artifactInfo.getRepository(),
                                                         artifactCoordinates,
                                                         url);
            results.add(result);
        }

        return results;
    }

    public String getURLForArtifact(String storageId,
                                    String repositoryId,
                                    String pathToArtifactFile)
    {
        String baseUrl = getConfiguration().getBaseUrl();
        baseUrl = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/");

        return baseUrl + "storages/" + storageId + "/" + repositoryId + "/" + pathToArtifactFile;
    }

    public void close()
            throws IOException
    {
        indexer.closeIndexingContext(indexingContext, false);
    }

    public void close(boolean deleteFiles)
            throws IOException
    {
        indexingContext.close(deleteFiles);
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

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public RepositoryPath getIndexDir()
    {
        return indexDir;
    }

    public void setIndexDir(RepositoryPath indexDir)
    {
        this.indexDir = indexDir;
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public String getContextId()
    {
        return contextId;
    }

    public void setContextId(String contextId)
    {
        this.contextId = contextId;
    }

    public void setApplicationContext(final ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }
}
