package org.carlspring.strongbox.testing;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.config.StorageIndexingConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.RemoteRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author carlspring
 */
@ContextConfiguration
public abstract class TestCaseWithArtifactGenerationWithIndexing
        extends TestCaseWithArtifactGeneration
{

    public static final int ROUTING_RULE_TYPE_DENIED = 0;

    public static final int ROUTING_RULE_TYPE_ACCEPTED = 1;


    @Configuration
    @Import({ StorageIndexingConfig.class,
              StorageApiConfig.class,
              CommonConfig.class,
              ClientConfig.class,
              DataServiceConfig.class
            })
    public static class SpringConfig { }

    @Autowired
    protected RepositoryIndexManager repositoryIndexManager;

    @Autowired
    protected ConfigurationManagementService configurationManagementService;

    @Autowired
    protected RepositoryManagementService repositoryManagementService;

    @Autowired
    protected ArtifactSearchService artifactSearchService;


    @Before
    public void init()
            throws Exception
    {
        if (getRepositoriesToClean() != null)
        {
            for (Map.Entry<String, String> entry : getRepositoriesToClean().entrySet())
            {
                String storageId = entry.getKey();
                String repositoryId = entry.getValue();

                removeRepositoryDirectory(storageId, repositoryId);
            }
        }
    }

    public abstract Map<String, String> getRepositoriesToClean();

    private void removeRepositoryDirectory(String storageId,
                                           String repositoryId)
            throws IOException
    {
        File repositoryBaseDir = new File(ConfigurationResourceResolver.getVaultDirectory(),
                                          "/storages/" + storageId + "/" + repositoryId);

        if (repositoryBaseDir.exists())
        {
            FileUtils.deleteDirectory(repositoryBaseDir);
        }
    }

    protected void createTestRepositoryWithArtifacts(Repository repository,
                                                     String ga,
                                                     String... versions)
            throws IOException,
                   JAXBException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        createRepository(repository);
        generateArtifactsReIndexAndPack(repository.getStorage().getId(), repository.getId(), ga, versions);
    }

    protected void createTestRepositoryWithArtifacts(String storageId,
                                                     String repositoryId,
                                                     boolean indexing,
                                                     String ga,
                                                     String... versions)
            throws IOException,
                   JAXBException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        createTestRepository(storageId, repositoryId, indexing);
        generateArtifactsReIndexAndPack(storageId, repositoryId, ga, versions);
    }

    protected void createTestRepository(String storageId,
                                        String repositoryId,
                                        boolean indexing)
            throws IOException, JAXBException
    {
        Repository repository = new Repository(repositoryId);
        repository.setIndexingEnabled(indexing);
        repository.setStorage(configurationManagementService.getStorage(storageId));

        createRepository(repository);
    }

    protected void createTestProxyRepository(String storageId,
                                             String repositoryId,
                                             String proxyUrl)
            throws IOException, JAXBException
    {
        RemoteRepository remoteRepository = new RemoteRepository();
        remoteRepository.setUrl(proxyUrl);

        Repository repository = new Repository(repositoryId);
        repository.setRemoteRepository(remoteRepository);
        repository.setIndexingEnabled(true);
        repository.setStorage(configurationManagementService.getStorage(storageId));

        createRepository(repository);
    }

    protected void createRepository(Repository repository)
            throws IOException,
                   JAXBException
    {
        repository.setIndexingEnabled(true);

        configurationManagementService.addOrUpdateRepository(repository.getStorage().getId(), repository);

        // Create the repository
        repositoryManagementService.createRepository(repository.getStorage().getId(), repository.getId());
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

        repositoryManagementService.reIndex(storageId, repositoryId, ga.replaceAll("\\.", "/").replaceAll("\\:", "\\/"));
        repositoryManagementService.pack(storageId, repositoryId);
    }

    public void addArtifactToIndex(File repositoryBasedir,
                                   String storageId,
                                   String repositoryId,
                                   String artifactPath)
            throws IOException
    {
        File artifactFile = new File(repositoryBasedir, artifactPath);

        Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactPath);

        RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndexer(storageId + ":" + repositoryId);

        indexer.addArtifactToIndex(repositoryId, artifactFile, artifact);
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
            configurationManagementService.addOrUpdateAcceptedRuleSet(ruleSet);
        }
        else
        {
            routingRules.addDenyRule(groupRepositoryId, ruleSet);
            configurationManagementService.addOrUpdateDeniedRuleSet(ruleSet);
        }
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
