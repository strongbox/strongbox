package org.carlspring.strongbox.storage.indexing;

import static java.util.Arrays.asList;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.FlatSearchRequest;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.Scanner;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.expr.SourcedSearchExpression;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryIndexer
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryIndexer.class);

    private static final Version luceneVersion = Version.LUCENE_48;

    private static final String[] luceneFields = new String[]{ "g",
                                                               "a",
                                                               "v",
                                                               "p",
                                                               "c" };

    private static final WhitespaceAnalyzer luceneAnalyzer = new WhitespaceAnalyzer(luceneVersion);

    private Indexer indexer;

    private Scanner scanner;

    private List<IndexCreator> indexers;

    private IndexingContext indexingContext;

    private String storageId;

    private String repositoryId;

    private File repositoryBasedir;

    private File indexDir;

    private IndexerConfiguration indexerConfiguration;

    private Configuration configuration;

    private String contextId;


    public RepositoryIndexer(String contextId)
    {
        this.contextId = contextId;
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

    public void delete(final Collection<ArtifactInfo> artifactInfos)
            throws IOException
    {
        final List<ArtifactContext> delete = new ArrayList<>();
        for (final ArtifactInfo artifactInfo : artifactInfos)
        {
            logger.debug("Deleting artifact: {}; ctx id: {}; idx dir: {}",
                         new String[]{ artifactInfo.getGroupId() + ":" +
                                       artifactInfo.getArtifactId() + ":" +
                                       artifactInfo.getVersion() + ":" +
                                       artifactInfo.getClassifier() + ":" +
                                       artifactInfo.getFileExtension(),
                                       indexingContext.getId(),
                                       indexingContext.getIndexDirectory().toString() });

            delete.add(new ArtifactContext(null, null, null, artifactInfo, null));
        }

        getIndexer().deleteArtifactsFromIndex(delete, indexingContext);
    }

    public Set<SearchResult> search(final String groupId,
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
            final Query query = new MultiFieldQueryParser(luceneVersion, luceneFields, luceneAnalyzer).parse(queryText);

            logger.debug("Text of the query: {}", queryText);
            logger.debug("Executing search query: {}; ctx id: {}; idx dir: {}",
                         new String[]{ query.toString(),
                                       indexingContext.getId(),
                                       indexingContext.getIndexDirectory().toString() });

            FlatSearchRequest searchRequest = new FlatSearchRequest(query, (a1,
                                                                            a2) -> calculateArtifactInfo(a1).compareTo(calculateArtifactInfo(a2)),
                    indexingContext);
            
            final FlatSearchResponse response = getIndexer().searchFlat(searchRequest);

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
        catch (Exception e)
        {
            logger.warn("Unable execute search query", e);
            return new HashSet<>();
        }
    }

    protected String calculateArtifactInfo(ArtifactInfo a1)
    {
        return a1.getGroupId()+a1.getArtifactId()+a1.getVersion();
    }

    public Set<SearchResult> searchBySHA1(final String checksum)
            throws IOException
    {
        final BooleanQuery query = new BooleanQuery();
        query.add(getIndexer().constructQuery(MAVEN.SHA1, new SourcedSearchExpression(checksum)), MUST);

        logger.debug("Executing search query: {}; ctx id: {}; idx dir: {}",
                     new String[]{ query.toString(),
                                   indexingContext.getId(),
                                   indexingContext.getIndexDirectory().toString() });

        final FlatSearchResponse response = getIndexer().searchFlat(new FlatSearchRequest(query, indexingContext));

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
            Artifact artifact = new DefaultArtifact(artifactInfo.getGroupId(),
                                                    artifactInfo.getArtifactId(),
                                                    artifactInfo.getVersion(),
                                                    "compile",
                                                    artifactInfo.getFileExtension(),
                                                    artifactInfo.getClassifier(),
                                                    // This particular part is not quite smart, but should do:
                                                    new DefaultArtifactHandler(artifactInfo.getFileExtension()));

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

    public void addArtifactToIndex(String repositoryId,
                                   File artifactFile,
                                   Artifact artifact)
            throws IOException
    {
        try
        {
            String extension = artifactFile.getName().substring(artifactFile.getName().lastIndexOf(".") + 1,
                                                                artifactFile.getName().length());

            ArtifactInfo artifactInfo = new ArtifactInfo(repositoryId,
                                                         artifact.getGroupId(),
                                                         artifact.getArtifactId(),
                                                         artifact.getVersion(),
                                                         obtainClassifier(artifact),
                                                         extension);

            if (artifact.getType() != null)
            {
                artifactInfo.setFieldValue(MAVEN.PACKAGING, artifact.getType());
            }

            logger.debug("Adding artifact: {}; repo: {}; type: {}", new String[]{ artifact.getGroupId() + ":" +
                                                                                  artifact.getArtifactId() + ":" +
                                                                                  artifact.getVersion() + ":" +
                                                                                  artifactInfo.getClassifier() + ":" +
                                                                                  extension,
                                                                                  repositoryId,
                                                                                  artifact.getType() });

            File pomFile = new File(artifactFile.getAbsolutePath() + ".pom");
            // TODO: Improve this to support timestamped SNAPSHOT-s:
            File metadataFile = new File(artifactFile.getParentFile().getParentFile(), "maven-metadata.xml");

            getIndexer().addArtifactsToIndex(asList(new ArtifactContext(pomFile.exists() ? pomFile : null,
                                                                        artifactFile,
                                                                        metadataFile.exists() ? metadataFile : null,
                                                                        artifactInfo,
                                                                        artifactInfo.calculateGav())),
                                             indexingContext);
        }
        catch (Exception e) // it's not really a critical problem, artifacts could be added to index later
        {
            logger.warn("Unable to add artifacts to index", e);
        }
    }

    private String obtainClassifier(Artifact artifactInfo)
    {
        String classifier = artifactInfo.getClassifier();
        if (classifier == null || classifier.isEmpty() || classifier.equalsIgnoreCase("null"))
        {
            return null;
        }
        return classifier;
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

}
