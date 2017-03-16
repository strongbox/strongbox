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
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.RemoteRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author carlspring
 */
@ContextConfiguration
public abstract class TestCaseWithArtifactGenerationAndIndexing
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

    @Inject
    protected RepositoryIndexManager repositoryIndexManager;

    @Inject
    protected ConfigurationManagementService configurationManagementService;

    @Inject
    protected RepositoryManagementService repositoryManagementService;

    @Inject
    protected ArtifactSearchService artifactSearchService;

    @Inject
    protected StorageManagementService storageManagementService;


    public static void cleanUp(Set<Repository> repositoriesToClean)
            throws Exception
    {
        if (repositoriesToClean != null)
        {
            for (Repository repository : repositoriesToClean)
            {
                removeRepositoryDirectory(repository.getStorage().getId(), repository.getId());
            }
        }
    }

    private static void removeRepositoryDirectory(String storageId,
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

    public void removeRepositories(Set<Repository> repositoriesToClean)
            throws IOException, JAXBException
    {
        for (Repository repository : repositoriesToClean)
        {
            configurationManagementService.removeRepository(repository.getStorage().getId(), repository.getId());
            if (repository.isIndexingEnabled())
            {
                repositoryIndexManager.closeIndexersForRepository(repository.getStorage().getId(), repository.getId());
            }
        }
    }

    public static Repository createRepositoryMock(String storageId,
                                                  String repositoryId)
    {
        // This is no the real storage, but has a matching ID.
        // We're mocking it, as the configurationManager is not available at the the static methods are invoked.
        Storage storage = new Storage(storageId);

        Repository repository = new Repository(repositoryId);
        repository.setStorage(storage);

        return repository;
    }

    protected void createRepositoryWithArtifacts(Repository repository,
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
        createRepository(storageId, repositoryId, null, indexing);
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
        configurationManagementService.saveRepository(repository.getStorage()
                                                                .getId(), repository);

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

        if (configurationManagementService.getConfiguration().getStorage(storageId).getRepository(repositoryId).isIndexingEnabled())
        {
            repositoryManagementService.reIndex(storageId, repositoryId, ga.replaceAll("\\.", "/").replaceAll("\\:", "\\/"));
            repositoryManagementService.pack(storageId, repositoryId);
        }
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
            configurationManagementService.saveAcceptedRuleSet(ruleSet);
        }
        else
        {
            routingRules.addDenyRule(groupRepositoryId, ruleSet);
            configurationManagementService.saveDeniedRuleSet(ruleSet);
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
