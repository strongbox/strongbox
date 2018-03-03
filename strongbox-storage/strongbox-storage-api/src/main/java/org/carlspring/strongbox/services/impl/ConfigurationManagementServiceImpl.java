package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.BinaryConfiguration;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationFileManager;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.services.BinaryConfigurationService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.support.ConfigurationReadException;
import org.carlspring.strongbox.services.support.ConfigurationSaveException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.HttpConnectionPool;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.orientechnologies.orient.core.entity.OEntityManager;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author mtodorov
 */
@Transactional
@Service
public class ConfigurationManagementServiceImpl
        implements ConfigurationManagementService
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementService.class);

    private final GenericParser<Configuration> parser = new GenericParser<>(Configuration.class);

    @Inject
    private BinaryConfigurationService binaryConfigurationService;

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @Inject
    private ConfigurationFileManager configurationFileManager;

    @Inject
    private OEntityManager oEntityManager;

    @PostConstruct
    void init()
    {
        final Configuration configuration;
        try
        {
            configuration = configurationFileManager.read();
        }
        catch (JAXBException | IOException ex)
        {
            throw new ConfigurationReadException(ex);
        }

        postRead(configuration);
        setProxyRepositoryConnectionPoolConfigurations(configuration);
        dump(configuration);

        oEntityManager.registerEntityClass(BinaryConfiguration.class);
        save(configuration);
    }

    private void postRead(final Configuration configuration)
    {
        setRepositoryStorageRelationships(configuration);
        setAllows(configuration);
    }

    private void setAllows(final Configuration configuration)
    {
        final Map<String, Storage> storages = configuration.getStorages();

        if (storages != null && !storages.isEmpty())
        {
            for (Storage storage : storages.values())
            {
                if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                {
                    for (Repository repository : storage.getRepositories().values())
                    {
                        if (repository.getType().equals(RepositoryTypeEnum.GROUP.getType()))
                        {
                            repository.setAllowsDelete(false);
                            repository.setAllowsDeployment(false);
                            repository.setAllowsRedeployment(false);
                        }
                        if (repository.getType().equals(RepositoryTypeEnum.PROXY.getType()))
                        {
                            repository.setAllowsDeployment(false);
                            repository.setAllowsRedeployment(false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets the repository <--> storage relationships explicitly, as initially, when these are deserialized from the
     * XML, they have no such relationship.
     *
     * @param configuration
     */
    private void setRepositoryStorageRelationships(final Configuration configuration)
    {
        final Map<String, Storage> storages = configuration.getStorages();

        if (storages != null && !storages.isEmpty())
        {
            for (Storage storage : storages.values())
            {
                if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                {
                    for (Repository repository : storage.getRepositories().values())
                    {
                        repository.setStorage(storage);
                    }
                }
            }
        }
    }

    private void setProxyRepositoryConnectionPoolConfigurations(final Configuration configuration)
    {
        configuration.getStorages().values().stream()
                     .filter(storage -> MapUtils.isNotEmpty(storage.getRepositories()))
                     .flatMap(storage -> storage.getRepositories().values().stream())
                     .forEach(repository ->
                              {
                                  if (repository.getHttpConnectionPool() != null
                                      && repository.getRemoteRepository() != null &&
                                      repository.getRemoteRepository().getUrl() != null)
                                  {
                                      proxyRepositoryConnectionPoolConfigurationService.setMaxPerRepository(
                                              repository.getRemoteRepository().getUrl(),
                                              repository.getHttpConnectionPool().getAllocatedConnections());
                                  }
                              });
    }

    private void dump(final Configuration configuration)
    {
        if (!configuration.getStorages().isEmpty())
        {
            logger.info("Loading storages...");
            for (String storageKey : configuration.getStorages().keySet())
            {
                logger.info(" -> Storage: " + storageKey);
                if (storageKey == null)
                {
                    throw new IllegalArgumentException("Null keys do not supported");
                }

                Storage storage = configuration.getStorages().get(storageKey);
                for (String repositoryKey : storage.getRepositories().keySet())
                {
                    logger.info("    -> Repository: " + repositoryKey);
                }
            }
        }
    }

    @Override
    public void save(Configuration configuration)
    {
        final BinaryConfiguration binaryConfiguration = binaryConfigurationService.findOne().orElse(
                new BinaryConfiguration());
        if (configuration.getUuid() != null)
        {
            // TODO https://youtrack.carlspring.org/issue/SB-930
            configuration.copyTrackingFields(binaryConfiguration);
        }

        try
        {
            // TODO benchmark serialization
            final String data = serializeToString(configuration);
            binaryConfiguration.setData(data);
            binaryConfigurationService.save(binaryConfiguration);

            configurationFileManager.store(configuration);
        }
        catch (JAXBException | IOException ex)
        {
            throw new ConfigurationSaveException(ex);
        }
    }

    @Override
    public Configuration getConfiguration()
    {
        final Optional<BinaryConfiguration> maybeConfiguration = binaryConfigurationService.findOne();
        if (!maybeConfiguration.isPresent())
        {
            return null;
        }

        try
        {
            final BinaryConfiguration binaryConfiguration = maybeConfiguration.get();
            // TODO benchmark deserialization
            final Configuration configuration = parser.deserialize(binaryConfiguration.getData());
            // TODO https://youtrack.carlspring.org/issue/SB-930
            binaryConfiguration.copyTrackingFields(configuration);
            postRead(configuration);
            return configuration;
        }
        catch (JAXBException e)
        {
            throw new ConfigurationReadException(e);
        }
    }

    @Override
    public void setConfiguration(Configuration configuration)
    {
        Objects.requireNonNull(configuration, "Configuration cannot be null");

        save(configuration);
    }

    @Override
    public String getBaseUrl()
    {
        return getConfiguration().getBaseUrl();
    }

    @Override
    public void setBaseUrl(String baseUrl)
    {
        Configuration configuration = getConfiguration();
        configuration.setBaseUrl(baseUrl);

        save(configuration);
    }

    @Override
    public int getPort()
    {
        return getConfiguration().getPort();
    }

    @Override
    public void setPort(int port)
    {
        Configuration configuration = getConfiguration();
        configuration.setPort(port);

        save(configuration);
    }

    @Override
    public void setProxyConfiguration(String storageId,
                                      String repositoryId,
                                      ProxyConfiguration proxyConfiguration)
    {
        Configuration configuration = getConfiguration();
        if (storageId != null && repositoryId != null)
        {
            configuration.getStorage(storageId)
                         .getRepository(repositoryId)
                         .setProxyConfiguration(proxyConfiguration);
        }
        else
        {
            configuration.setProxyConfiguration(proxyConfiguration);
        }

        save(configuration);
    }

    @Override
    public ProxyConfiguration getProxyConfiguration()
    {
        return getConfiguration().getProxyConfiguration();
    }

    @Override
    public void saveStorage(Storage storage)
    {
        Configuration configuration = getConfiguration();
        configuration.addStorage(storage);

        save(configuration);
    }

    @Override
    public Storage getStorage(String storageId)
    {
        return getConfiguration().getStorage(storageId);
    }

    @Override
    public void removeStorage(String storageId)
    {
        Configuration configuration = getConfiguration();
        configuration.getStorages().remove(storageId);

        save(configuration);
    }

    @Override
    public void saveRepository(String storageId,
                               Repository repository)
    {
        Configuration configuration = getConfiguration();
        configuration.getStorage(storageId)
                     .addRepository(repository);

        save(configuration);
    }

    @Override
    public Repository getRepository(String storageId,
                                    String repositoryId)
    {
        return getConfiguration().getStorage(storageId).getRepository(repositoryId);
    }

    @Override
    public List<Repository> getRepositoriesWithLayout(String storageId,
                                                      String layout)
    {
        Configuration configuration = getConfiguration();
        Stream<Repository> repositories;
        if (storageId != null)
        {
            Storage storage = configuration.getStorage(storageId);
            if (storage != null)
            {
                repositories = storage.getRepositories().values().stream();
            }
            else
            {
                return Collections.emptyList();
            }
        }
        else
        {
            repositories = configuration.getStorages().values().stream()
                                        .flatMap(storage -> storage.getRepositories().values().stream());
        }

        return repositories.filter(repository -> repository.getLayout().equals(layout))
                           .collect(Collectors.toList());
    }

    @Override
    public List<Repository> getGroupRepositories()
    {
        List<Repository> groupRepositories = new ArrayList<>();

        for (Storage storage : getConfiguration().getStorages().values())
        {
            groupRepositories.addAll(storage.getRepositories().values().stream()
                                            .filter(repository -> repository.getType()
                                                                            .equals(RepositoryTypeEnum.GROUP.getType()))
                                            .collect(Collectors.toList()));
        }

        return groupRepositories;
    }

    @Override
    public List<Repository> getGroupRepositoriesContaining(String storageId,
                                                           String repositoryId)
    {
        List<Repository> groupRepositories = new ArrayList<>();

        Storage storage = getConfiguration().getStorage(storageId);

        groupRepositories.addAll(storage.getRepositories().values().stream()
                                        .filter(repository -> repository.getType()
                                                                        .equals(RepositoryTypeEnum.GROUP.getType()))
                                        .filter(repository -> repository.getGroupRepositories()
                                                                        .contains(repositoryId))
                                        .collect(Collectors.toList()));


        return groupRepositories;
    }

    @Override
    public void removeRepositoryFromAssociatedGroups(String storageId,
                                                     String repositoryId)
    {
        List<Repository> includedInGroupRepositories = getGroupRepositoriesContaining(storageId, repositoryId);

        if (!includedInGroupRepositories.isEmpty())
        {
            Configuration configuration = getConfiguration();

            for (Repository repository : includedInGroupRepositories)
            {
                configuration.getStorage(repository.getStorage().getId())
                             .getRepository(repository.getId())
                             .getGroupRepositories().remove(repositoryId);
            }

            save(configuration);
        }
    }

    @Override
    public void removeRepository(String storageId,
                                 String repositoryId)
    {
        Configuration configuration = getConfiguration();
        configuration.getStorage(storageId).removeRepository(repositoryId);
        save(configuration);

        removeRepositoryFromAssociatedGroups(storageId, repositoryId);
    }

    @Override
    public void setProxyRepositoryMaxConnections(String storageId,
                                                 String repositoryId,
                                                 int numberOfConnections)
    {
        Configuration configuration = getConfiguration();
        Repository repository = configuration.getStorage(storageId).getRepository(repositoryId);
        if (repository.getHttpConnectionPool() == null)
        {
            repository.setHttpConnectionPool(new HttpConnectionPool());
        }

        repository.getHttpConnectionPool().setAllocatedConnections(numberOfConnections);

        save(configuration);
    }

    @Override
    public HttpConnectionPool getHttpConnectionPoolConfiguration(String storageId,
                                                                 String repositoryId)
    {
        Repository repository = getRepository(storageId, repositoryId);
        return repository.getHttpConnectionPool();
    }

    @Override
    public boolean saveAcceptedRuleSet(RuleSet ruleSet)
    {
        Configuration configuration = getConfiguration();
        configuration.getRoutingRules().addAcceptRule(ruleSet.getGroupRepository(), ruleSet);

        save(configuration);

        return true;
    }

    @Override
    public boolean saveDeniedRuleSet(RuleSet ruleSet)
    {
        Configuration configuration = getConfiguration();
        configuration.getRoutingRules().addDenyRule(ruleSet.getGroupRepository(), ruleSet);

        save(configuration);

        return true;
    }

    @Override
    public boolean removeAcceptedRuleSet(String groupRepository)
    {
        Configuration configuration = getConfiguration();
        final Map<String, RuleSet> accepted = configuration.getRoutingRules().getAccepted();
        boolean result = false;
        if (accepted.containsKey(groupRepository))
        {
            result = true;
            accepted.remove(groupRepository);
        }

        save(configuration);

        return result;
    }

    @Override
    public boolean saveAcceptedRepository(String groupRepository,
                                          RoutingRule routingRule)
    {
        Configuration configuration = getConfiguration();
        RoutingRules routingRules = configuration.getRoutingRules();

        logger.info("Routing rules: \n" + routingRules + "\nAccepted empty " + routingRules.getAccepted().isEmpty());

        final Map<String, RuleSet> acceptedRulesMap = routingRules.getAccepted();
        boolean added = false;
        if (acceptedRulesMap.containsKey(groupRepository))
        {
            for (RoutingRule rl : acceptedRulesMap.get(groupRepository).getRoutingRules())
            {
                if (routingRule.getPattern().equals(rl.getPattern()))
                {
                    added = true;
                    rl.getRepositories().addAll(routingRule.getRepositories());
                }
            }
        }

        save(configuration);

        return added;
    }

    @Override
    public boolean removeAcceptedRepository(String groupRepository,
                                            String pattern,
                                            String repositoryId)
    {
        Configuration configuration = getConfiguration();
        final Map<String, RuleSet> acceptedRules = configuration.getRoutingRules().getAccepted();
        boolean removed = false;
        if (acceptedRules.containsKey(groupRepository))
        {
            for (RoutingRule routingRule : acceptedRules.get(groupRepository).getRoutingRules())
            {
                if (pattern.equals(routingRule.getPattern()))
                {
                    removed = true;
                    routingRule.getRepositories().remove(repositoryId);
                }
            }
        }

        save(configuration);

        return removed;
    }

    @Override
    public boolean overrideAcceptedRepositories(String groupRepository,
                                                RoutingRule routingRule)
    {
        boolean overridden = false;
        Configuration configuration = getConfiguration();
        if (configuration.getRoutingRules().getAccepted().containsKey(groupRepository))
        {
            for (RoutingRule rule : configuration.getRoutingRules()
                                                 .getAccepted()
                                                 .get(groupRepository)
                                                 .getRoutingRules())
            {
                if (routingRule.getPattern().equals(rule.getPattern()))
                {
                    overridden = true;
                    rule.setRepositories(routingRule.getRepositories());
                }
            }
        }

        save(configuration);

        return overridden;
    }

    @Override
    public RoutingRules getRoutingRules()
    {
        return getConfiguration().getRoutingRules();
    }

    private String serializeToString(final Configuration configuration)
            throws JAXBException
    {
        return parser.serialize(configuration);
    }

}
