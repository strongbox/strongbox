package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;
import org.carlspring.strongbox.storage.search.SearchRequest;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
public abstract class TestCaseWithMavenArtifactGenerationAndIndexing
        extends TestCaseWithMavenArtifactGeneration
{

    private static final Logger logger = LoggerFactory.getLogger(TestCaseWithMavenArtifactGenerationAndIndexing.class);

    public static final int ROUTING_RULE_TYPE_DENIED = 0;

    public static final int ROUTING_RULE_TYPE_ACCEPTED = 1;

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
    protected StorageManagementService storageManagementService;


    protected void createRepositoryWithArtifacts(Repository repository,
                                                 String ga,
                                                 String... versions)
            throws IOException,
                   JAXBException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        createRepository(repository);
        generateArtifactsReIndexAndPack(repository.getStorage()
                                                  .getId(), repository.getId(), ga, versions);
    }

    protected void createRepositoryWithArtifacts(String storageId,
                                                 String repositoryId,
                                                 boolean indexing,
                                                 String ga,
                                                 String... versions)
            throws IOException,
                   JAXBException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        createRepository(storageId, repositoryId, indexing);
        generateArtifactsReIndexAndPack(storageId, repositoryId, ga, versions);
    }

    protected void createRepository(String storageId,
                                    String repositoryId,
                                    boolean indexing)
            throws IOException, JAXBException
    {
        createRepository(storageId, repositoryId, RepositoryPolicyEnum.RELEASE.getPolicy(), indexing);
    }


    protected void createRepository(String storageId,
                                    String repositoryId,
                                    String policy,
                                    boolean indexing)
            throws IOException, JAXBException
    {
        Repository repository = new Repository(repositoryId);
        repository.setIndexingEnabled(indexing);
        repository.setPolicy(policy);
        repository.setStorage(configurationManagementService.getStorage(storageId));

        createRepository(repository);
    }

    protected void createProxyRepository(String storageId,
                                         String repositoryId,
                                         String remoteRepositoryUrl)
            throws IOException, JAXBException
    {
        RemoteRepository remoteRepository = new RemoteRepository();
        remoteRepository.setUrl(remoteRepositoryUrl);

        Repository repository = new Repository(repositoryId);
        repository.setRemoteRepository(remoteRepository);
        repository.setIndexingEnabled(true);
        repository.setStorage(configurationManagementService.getStorage(storageId));

        createRepository(repository);
    }

    public void createRepository(Repository repository)
            throws IOException,
                   JAXBException
    {
        configurationManagementService.saveRepository(repository.getStorage().getId(), repository);

        // Create the repository
        repositoryManagementService.createRepository(repository.getStorage().getId(), repository.getId());
    }

    public void createStorage(String storageId)
            throws IOException, JAXBException
    {
        createStorage(new Storage(storageId));
    }

    public void createStorage(Storage storage)
            throws IOException, JAXBException
    {
        configurationManagementService.saveStorage(storage);
        storageManagementService.createStorage(storage);
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

        if (configurationManagementService.getConfiguration()
                                          .getStorage(storageId)
                                          .getRepository(repositoryId)
                                          .isIndexingEnabled())
        {
            Storage storage = configurationManager.getConfiguration()
                                                  .getStorage(storageId);
            Repository repository = storage.getRepository(repositoryId);

            LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
            MavenRepositoryFeatures features = (MavenRepositoryFeatures) layoutProvider.getRepositoryFeatures();

            features.reIndex(storageId, repositoryId, ga.replaceAll("\\.", "/")
                                                        .replaceAll("\\:", "\\/"));
            features.pack(storageId, repositoryId);
        }
    }

    public void reIndex(String storageId,
                        String repositoryId,
                        String path)
            throws IOException
    {
        if (configurationManagementService.getConfiguration()
                                          .getStorage(storageId)
                                          .getRepository(repositoryId)
                                          .isIndexingEnabled())
        {
            Storage storage = configurationManager.getConfiguration()
                                                  .getStorage(storageId);
            Repository repository = storage.getRepository(repositoryId);

            LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
            MavenRepositoryFeatures features = (MavenRepositoryFeatures) layoutProvider.getRepositoryFeatures();

            features.reIndex(storageId, repositoryId, path != null ? path : ".");
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
