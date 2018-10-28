package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.locator.handlers.GenerateMavenMetadataOperation;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.repository.remote.MutableRemoteRepository;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;
import org.carlspring.strongbox.storage.routing.MutableRoutingRules;
import org.carlspring.strongbox.storage.routing.MutableRuleSet;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
public abstract class TestCaseWithMavenArtifactGenerationAndIndexing
        extends MavenTestCaseWithArtifactGeneration
{

    public static final int ROUTING_RULE_TYPE_DENIED = 0;

    public static final int ROUTING_RULE_TYPE_ACCEPTED = 1;

    private static final Logger logger = LoggerFactory.getLogger(TestCaseWithMavenArtifactGenerationAndIndexing.class);

    @Inject
    protected Optional<RepositoryIndexManager> repositoryIndexManager;

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

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;


    protected void createRepositoryWithArtifacts(String storageId,
                                                 MutableRepository repository,
                                                 String ga,
                                                 String... versions)
            throws IOException,
                   JAXBException,
                   NoSuchAlgorithmException,
                   XmlPullParserException,
                   RepositoryManagementStrategyException
    {
        createRepository(storageId, repository);
        generateArtifactsReIndexAndPack(repository.getStorage().getId(), repository.getId(), ga, versions);
    }

    protected void createRepositoryWithArtifacts(String storageId,
                                                 String repositoryId,
                                                 boolean indexing,
                                                 String ga,
                                                 String... versions)
            throws Exception
    {
        MutableMavenRepositoryConfiguration repositoryConfiguration = new MutableMavenRepositoryConfiguration();
        repositoryConfiguration.setIndexingEnabled(indexing);

        createRepositoryWithArtifacts(storageId, repositoryId, repositoryConfiguration, ga, versions);
    }

    protected void createRepositoryWithArtifacts(String storageId,
                                                 String repositoryId,
                                                 MutableMavenRepositoryConfiguration repositoryConfiguration,
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
        MutableMavenRepositoryConfiguration repositoryConfiguration = new MutableMavenRepositoryConfiguration();
        repositoryConfiguration.setIndexingEnabled(indexing);

        createRepository(storageId, repositoryId, RepositoryPolicyEnum.RELEASE.getPolicy(), repositoryConfiguration);
    }

    protected void createRepository(String storageId,
                                    String repositoryId,
                                    MutableMavenRepositoryConfiguration repositoryConfiguration)
            throws IOException, JAXBException, RepositoryManagementStrategyException
    {
        createRepository(storageId, repositoryId, RepositoryPolicyEnum.RELEASE.getPolicy(), repositoryConfiguration);
    }

    protected MutableRepository createRepository(String storageId,
                                                 String repositoryId,
                                                 String policy,
                                                 boolean indexing)
        throws IOException,
        JAXBException,
        RepositoryManagementStrategyException
    {
        MutableMavenRepositoryConfiguration repositoryConfiguration = new MutableMavenRepositoryConfiguration();
        repositoryConfiguration.setIndexingEnabled(indexing);

        return createRepository(storageId, repositoryId, policy, repositoryConfiguration);
    }

    protected MutableRepository createRepository(String storageId,
                                                 String repositoryId,
                                                 String policy,
                                                 MutableMavenRepositoryConfiguration repositoryConfiguration)
        throws IOException,
        JAXBException,
        RepositoryManagementStrategyException
    {
        MutableRepository repository = mavenRepositoryFactory.createRepository(repositoryId);
        repository.setPolicy(policy);
        repository.setRepositoryConfiguration(repositoryConfiguration);
        repository.setBasedir(ConfigurationResourceResolver.getVaultDirectory()
                + String.format("/storages/%s/%s", storageId, repositoryId));
        
        createRepository(storageId, repository);
        
        return repository;
    }

    @Override
    public void createProxyRepository(String storageId,
                                      String repositoryId,
                                      String remoteRepositoryUrl)
            throws IOException,
                   JAXBException,
                   RepositoryManagementStrategyException
    {
        MutableMavenRepositoryConfiguration repositoryConfiguration = new MutableMavenRepositoryConfiguration();
        repositoryConfiguration.setIndexingEnabled(true);

        MutableRemoteRepository remoteRepository = new MutableRemoteRepository();
        remoteRepository.setUrl(remoteRepositoryUrl);

        MutableRepository repository = mavenRepositoryFactory.createRepository(repositoryId);
        repository.setRemoteRepository(remoteRepository);
        repository.setRepositoryConfiguration(repositoryConfiguration);
        repository.setType(RepositoryTypeEnum.PROXY.getType());

        createRepository(storageId, repository);
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

        if (!(features instanceof IndexedMavenRepositoryFeatures))
        {
            return;
        }

        IndexedMavenRepositoryFeatures indexedFeatures = (IndexedMavenRepositoryFeatures) features;

        if (indexedFeatures.isIndexingEnabled(repository))
        {
            indexedFeatures.reIndex(storageId, repositoryId, ga.replaceAll("\\.", "/").replaceAll("\\:", "\\/"));
            indexedFeatures.pack(storageId, repositoryId);
        }
    }

    public void reIndex(String storageId,
                        String repositoryId,
                        String path)
    {
        Repository repository = configurationManagementService.getConfiguration()
                                                              .getStorage(storageId)
                                                              .getRepository(repositoryId);

        if (!(features instanceof IndexedMavenRepositoryFeatures))
        {
            return;
        }

        IndexedMavenRepositoryFeatures indexedFeatures = (IndexedMavenRepositoryFeatures) features;

        if (indexedFeatures.isIndexingEnabled(repository))
        {
            indexedFeatures.reIndex(storageId, repositoryId, path != null ? path : ".");
        }
    }

    public void packIndex(String storageId,
                          String repositoryId)
            throws IOException
    {
        Repository repository = configurationManagementService.getConfiguration()
                                                              .getStorage(storageId)
                                                              .getRepository(repositoryId);

        if (!(features instanceof IndexedMavenRepositoryFeatures))
        {
            return;
        }

        IndexedMavenRepositoryFeatures indexedFeatures = (IndexedMavenRepositoryFeatures) features;

        if (indexedFeatures.isIndexingEnabled(repository))
        {
            indexedFeatures.pack(storageId, repositoryId);
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

        MutableRoutingRule routingRule = new MutableRoutingRule(rulePattern, repositories);

        List<MutableRoutingRule> routingRulesList = new ArrayList<>();
        routingRulesList.add(routingRule);

        MutableRuleSet ruleSet = new MutableRuleSet();
        ruleSet.setGroupRepository(groupRepositoryId);
        ruleSet.setRoutingRules(routingRulesList);

        MutableRoutingRules routingRules = new MutableRoutingRules();

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
        if (!repositoryIndexManager.isPresent())
        {
            return;
        }

        String contextId = storageId + ":" + repositoryId + ":" + indexType;
        RepositoryIndexer repositoryIndexer = repositoryIndexManager.get().getRepositoryIndexer(contextId);
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
        
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

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
        RepositoryPath repositoryPath = artifactResolutionService.resolvePath(storageId,
                                                                              repositoryId,
                                                                              path);

        try (final InputStream is = artifactResolutionService.getInputStream(repositoryPath))
        {
            assertNotNull("Failed to resolve " + path + "!", is);

            if (RepositoryFiles.isMetadata(repositoryPath))
            {
                System.out.println(ByteStreams.toByteArray(is));
            }
            else
            {
                while (is.read(new byte[1024]) != -1);
            }
        }
    }

    public void assertIndexContainsArtifact(String storageId,
                                            String repositoryId,
                                            String query)
            throws SearchException
    {
        Assume.assumeTrue(repositoryIndexManager.isPresent());

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

    protected void closeIndexersForRepository(String storageId,
                                              String repositoryId)
            throws IOException
    {
        if (repositoryIndexManager.isPresent())
        {
            repositoryIndexManager.get().closeIndexersForRepository(storageId, repositoryId);
        }
    }

    public void closeIndexer(String contextId)
            throws IOException
    {
        if (repositoryIndexManager.isPresent())
        {
            repositoryIndexManager.get().closeIndexer(contextId);
        }
    }

    public RepositoryManagementService getRepositoryManagementService()
    {
        return repositoryManagementService;
    }

    @Override
    public void removeRepositories(Set<MutableRepository> repositoriesToClean)
        throws IOException,
        JAXBException
    {
        for (MutableRepository mutableRepository : repositoriesToClean)
        {
            RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(new Repository(mutableRepository));

            Files.delete(repositoryPath);
        }
        
        super.removeRepositories(repositoriesToClean);
        
    }

    
}
