package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationFileManager;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.data.service.SingletonCrudService;
import org.carlspring.strongbox.data.service.SingletonEntityProvider;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.ConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.HttpConnectionPool;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author mtodorov
 */
@Transactional
@Service
public class ConfigurationManagementServiceImpl
        extends SingletonEntityProvider<Configuration, String>
        implements ConfigurationManagementService
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementService.class);

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private ConfigurationFileManager configurationFileManager;

    @Override
    public SingletonCrudService<Configuration, String> getService()
    {
        return configurationService;
    }

    @Override
    protected void postSave(final Configuration configuration)
    {
        configurationFileManager.store(configuration);
    }

    @Override
    protected void postGet(final Configuration configuration)
    {
        setRepositoryStorageRelationships(configuration);
        setAllows(configuration);
    }

    @Override
    public Configuration getConfiguration()
    {
        return get().orElse(null);
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

}
