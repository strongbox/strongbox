package org.carlspring.strongbox.testing;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.locator.handlers.GenerateMavenMetadataOperation;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.*;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.Bits;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.context.IndexUtils;
import org.apache.maven.index.context.IndexingContext;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
public abstract class TestCaseWithMavenArtifactGenerationAndIndexing
        extends TestCaseWithMavenArtifactGeneration
{

    public static final int ROUTING_RULE_TYPE_DENIED = 0;

    public static final int ROUTING_RULE_TYPE_ACCEPTED = 1;

    private static final Logger logger = LoggerFactory.getLogger(TestCaseWithMavenArtifactGenerationAndIndexing.class);

    @Inject
    protected RepositoryIndexManager repositoryIndexManager;

    @Inject
    protected ConfigurationManagementService configurationManagementService;

    @Inject
    protected ConfigurationManager configurationManager;

    @Inject
    protected RepositoryManagementService repositoryManagementService;

    @Inject
    protected ArtifactSearchService artifactSearchService;

    @Inject
    protected LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    protected MavenMetadataManager mavenMetadataManager;

    @Inject
    protected ArtifactResolutionService artifactResolutionService;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;

    protected void createRepositoryWithArtifacts(Repository repository,
                                                 String ga,
                                                 String... versions)
            throws IOException,
                   JAXBException,
                   NoSuchAlgorithmException,
                   XmlPullParserException,
                   RepositoryManagementStrategyException
    {
        createRepository(repository);
        generateArtifactsReIndexAndPack(repository.getStorage().getId(), repository.getId(), ga, versions);
    }

    protected void createRepositoryWithArtifacts(String storageId,
                                                 String repositoryId,
                                                 boolean indexing,
                                                 String ga,
                                                 String... versions)
            throws Exception
    {
        MavenRepositoryConfiguration repositoryConfiguration = new MavenRepositoryConfiguration();
        repositoryConfiguration.setIndexingEnabled(indexing);

        createRepositoryWithArtifacts(storageId, repositoryId, repositoryConfiguration, ga, versions);
    }

    protected void createRepositoryWithArtifacts(String storageId,
                                                 String repositoryId,
                                                 MavenRepositoryConfiguration repositoryConfiguration,
                                                 String ga,
                                                 String... versions)
            throws Exception
    {
        createRepository(storageId, repositoryId, repositoryConfiguration);
        generateArtifactsReIndexAndPack(storageId, repositoryId, ga, versions);
    }

    protected void createRepository(String storageId,
                                    String repositoryId,
                                    boolean indexing)
            throws IOException, JAXBException, RepositoryManagementStrategyException
    {
        MavenRepositoryConfiguration repositoryConfiguration = new MavenRepositoryConfiguration();
        repositoryConfiguration.setIndexingEnabled(indexing);

        createRepository(storageId, repositoryId, RepositoryPolicyEnum.RELEASE.getPolicy(), repositoryConfiguration);
    }

    protected void createRepository(String storageId,
                                    String repositoryId,
                                    MavenRepositoryConfiguration repositoryConfiguration)
            throws IOException, JAXBException, RepositoryManagementStrategyException
    {
        createRepository(storageId, repositoryId, RepositoryPolicyEnum.RELEASE.getPolicy(), repositoryConfiguration);
    }

    protected void createRepository(String storageId,
                                    String repositoryId,
                                    String policy,
                                    boolean indexing)
            throws IOException, JAXBException, RepositoryManagementStrategyException
    {
        MavenRepositoryConfiguration repositoryConfiguration = new MavenRepositoryConfiguration();
        repositoryConfiguration.setIndexingEnabled(indexing);

        createRepository(storageId, repositoryId, policy, repositoryConfiguration);
    }

    protected void createRepository(String storageId,
                                    String repositoryId,
                                    String policy,
                                    MavenRepositoryConfiguration repositoryConfiguration)
            throws IOException, JAXBException, RepositoryManagementStrategyException
    {
        Repository repository = new Repository(repositoryId);
        repository.setPolicy(policy);
        repository.setStorage(configurationManagementService.getStorage(storageId));
        repository.setRepositoryConfiguration(repositoryConfiguration);

        createRepository(repository);
    }

    protected void createProxyRepository(String storageId,
                                         String repositoryId,
                                         String remoteRepositoryUrl)
            throws IOException, JAXBException, RepositoryManagementStrategyException
    {
        MavenRepositoryConfiguration repositoryConfiguration = new MavenRepositoryConfiguration();
        repositoryConfiguration.setIndexingEnabled(true);

        RemoteRepository remoteRepository = new RemoteRepository();
        remoteRepository.setUrl(remoteRepositoryUrl);

        Repository repository = new Repository(repositoryId);
        repository.setRemoteRepository(remoteRepository);
        repository.setStorage(configurationManagementService.getStorage(storageId));
        repository.setRepositoryConfiguration(repositoryConfiguration);

        createRepository(repository);
    }

    private void generateArtifactsReIndexAndPack(String storageId,
                                                 String repositoryId,
                                                 String ga,
                                                 String[] versions)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        for (String version : versions)
        {
            String repositoryBaseDir = ConfigurationResourceResolver.getVaultDirectory() +
                                       "/storages/" + storageId + "/" + repositoryId;

            generateArtifact(repositoryBaseDir, ga + ":" + version + ":jar");
        }

        Repository repository = configurationManagementService.getConfiguration()
                                                              .getStorage(storageId)
                                                              .getRepository(repositoryId);

        if (features.isIndexingEnabled(repository))
        {
            features.reIndex(storageId, repositoryId, ga.replaceAll("\\.", "/").replaceAll("\\:", "\\/"));
            features.pack(storageId, repositoryId);
        }
    }

    public void reIndex(String storageId,
                        String repositoryId,
                        String path)
            throws IOException
    {
        Repository repository = configurationManagementService.getConfiguration()
                                                              .getStorage(storageId)
                                                              .getRepository(repositoryId);

        if (features.isIndexingEnabled(repository))
        {
            features.reIndex(storageId, repositoryId, path != null ? path : ".");
        }
    }

    public void packIndex(String storageId,
                          String repositoryId)
            throws IOException
    {
        Repository repository = configurationManagementService.getConfiguration()
                                                              .getStorage(storageId)
                                                              .getRepository(repositoryId);

        if (features.isIndexingEnabled(repository))
        {
            features.pack(storageId, repositoryId);
        }
    }

    public void createRoutingRuleSet(String storageId,
                                     String groupRepositoryId,
                                     String[] repositoryIds,
                                     String rulePattern,
                                     int type)
    {
        Set<String> repositories = new LinkedHashSet<>();
        repositories.addAll(Arrays.asList(repositoryIds));

        RoutingRule routingRule = new RoutingRule(rulePattern, repositories);

        List<RoutingRule> routingRulesList = new ArrayList<>();
        routingRulesList.add(routingRule);

        RuleSet ruleSet = new RuleSet();
        ruleSet.setGroupRepository(groupRepositoryId);
        ruleSet.setRoutingRules(routingRulesList);

        RoutingRules routingRules = new RoutingRules();

        if (type == ROUTING_RULE_TYPE_ACCEPTED)
        {
            routingRules.addAcceptRule(groupRepositoryId, ruleSet);
            configurationManagementService.saveAcceptedRuleSet(ruleSet);
        }
        else
        {
            routingRules.addDenyRule(groupRepositoryId, ruleSet);
            configurationManagementService.saveDeniedRuleSet(ruleSet);
        }
    }

    public void dumpIndex(String storageId,
                          String repositoryId)
            throws IOException
    {
        dumpIndex(storageId, repositoryId, IndexTypeEnum.LOCAL.getType());
    }

    public void dumpIndex(String storageId,
                          String repositoryId,
                          String indexType)
            throws IOException
    {
        String contextId = storageId + ":" + repositoryId + ":" + indexType;
        RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndexer(contextId);
        if (repositoryIndexer == null)
        {
            logger.debug("Unable to find index for contextId " + contextId);
            return;
        }

        IndexingContext indexingContext = repositoryIndexer.getIndexingContext();

        final IndexSearcher searcher = indexingContext.acquireIndexSearcher();
        try
        {
            logger.debug("Dumping index for " + storageId + ":" + repositoryId + ":" + indexType + "...");

            final IndexReader ir = searcher.getIndexReader();
            Bits liveDocs = MultiFields.getLiveDocs(ir);
            for (int i = 0; i < ir.maxDoc(); i++)
            {
                if (liveDocs == null || liveDocs.get(i))
                {
                    final Document doc = ir.document(i);
                    final ArtifactInfo ai = IndexUtils.constructArtifactInfo(doc, indexingContext);
                    if (ai != null)
                    {
                        System.out.println("\t" + ai.toString());
                    }
                }
            }

            logger.debug("Index dump completed.");
        }
        finally
        {
            indexingContext.releaseIndexSearcher(searcher);
        }
    }

    protected void generateMavenMetadata(String storageId,
                                         String repositoryId)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        RepositoryPath repositoryPath = layoutProvider.resolve(repository);

        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setBasedir(repositoryPath);
        locator.setOperation(new GenerateMavenMetadataOperation(mavenMetadataManager, artifactEventListenerRegistry));
        locator.locateArtifactDirectories();
    }

    protected Path getVaultDirectoryPath()
    {
        String base = FilenameUtils.normalize(ConfigurationResourceResolver.getVaultDirectory());
        if (StringUtils.isBlank(base))
        {
            throw new IllegalStateException("ConfigurationResourceResolver.getVaultDirectory() resolves to '" + base +
                                            "' which is illegal base path here.");
        }
        return Paths.get(base);
    }

    protected void deleteDirectoryRelativeToVaultDirectory(String dirPathToDelete)
            throws Exception
    {
        Path basePath = getVaultDirectoryPath();
        Path fullDirPathToDelete = basePath.resolve(dirPathToDelete);
        FileUtils.deleteDirectory(fullDirPathToDelete.toFile());
    }

    protected void assertStreamNotNull(final String storageId,
                                     final String repositoryId,
                                     final String path)
            throws Exception
    {
        try (final InputStream is = artifactResolutionService.getInputStream(storageId, repositoryId, path))
        {
            assertNotNull("Failed to resolve " + path + "!", is);

            if (ArtifactUtils.isMetadata(path))
            {
                System.out.println(ByteStreams.toByteArray(is));
            }
        }
    }

    public void assertIndexContainsArtifact(String storageId,
                                            String repositoryId,
                                            String query)
            throws SearchException
    {
        boolean isContained = indexContainsArtifact(storageId, repositoryId, query);

        assertTrue(isContained);
    }

    public boolean indexContainsArtifact(String storageId,
                                         String repositoryId,
                                         String query)
            throws SearchException
    {
        SearchRequest request = new SearchRequest(storageId,
                                                  repositoryId,
                                                  query,
                                                  MavenIndexerSearchProvider.ALIAS);

        return artifactSearchService.contains(request);
    }

    public RepositoryIndexManager getRepositoryIndexManager()
    {
        return repositoryIndexManager;
    }

    public RepositoryManagementService getRepositoryManagementService()
    {
        return repositoryManagementService;
    }

}
