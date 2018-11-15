package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.client.MutableRemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.configuration.*;
import org.carlspring.strongbox.event.repository.RepositoryEvent;
import org.carlspring.strongbox.event.repository.RepositoryEventListenerRegistry;
import org.carlspring.strongbox.event.repository.RepositoryEventTypeEnum;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.*;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;
import org.carlspring.strongbox.storage.routing.MutableRuleSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author mtodorov
 */
@Service
public class ConfigurationManagementServiceImpl
        implements ConfigurationManagementService
{

    private final ReadWriteLock configurationLock = new ReentrantReadWriteLock();

    @Inject
    private ConfigurationFileManager configurationFileManager;

    @Inject
    private RepositoryEventListenerRegistry repositoryEventListenerRegistry;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @Inject
    private PlatformTransactionManager transactionManager;

    /**
     * Yes, this is a state object.
     * It is protected by the {@link #configurationLock} here
     * and should not be exposed to the world.
     *
     * @see #getConfiguration()
     */
    private MutableConfiguration configuration;

    @PostConstruct
    public void init()
    {
        new TransactionTemplate(transactionManager).execute((s) -> doInit());
    }

    private Object doInit()
    {
        final MutableConfiguration configuration = configurationFileManager.read();
        setConfiguration(configuration);
        setRepositoryArtifactCoordinateValidators();

        return null;
    }

    @Override
    public MutableConfiguration getMutableConfigurationClone()
    {
        final Lock readLock = configurationLock.readLock();
        readLock.lock();

        try
        {
            return SerializationUtils.clone(configuration);
        }
        catch (Exception e)
        {
            throw new UndeclaredThrowableException(e);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public Configuration getConfiguration()
    {
        final Lock readLock = configurationLock.readLock();
        readLock.lock();

        try
        {
            return new Configuration(configuration);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void setConfiguration(MutableConfiguration newConf)
    {
        Objects.requireNonNull(newConf, "Configuration cannot be null");

        modifyInLock(configuration ->
                     {
                         ConfigurationManagementServiceImpl.this.configuration = newConf;
                         setProxyRepositoryConnectionPoolConfigurations();
                         setRepositoryStorageRelationships();
                         setAllows();
                     });
    }

    @Override
    public void setInstanceName(String instanceName)
    {
        modifyInLock(configuration -> configuration.setInstanceName(instanceName));
    }

    @Override
    public void setBaseUrl(String baseUrl)
    {
        modifyInLock(configuration -> configuration.setBaseUrl(baseUrl));
    }

    @Override
    public void setPort(int port)
    {
        modifyInLock(configuration -> configuration.setPort(port));
    }

    @Override
    public void setProxyConfiguration(String storageId,
                                      String repositoryId,
                                      MutableProxyConfiguration proxyConfiguration)
    {
        modifyInLock(configuration ->
                     {
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
                     });
    }

    @Override
    public void saveStorage(MutableStorage storage)
    {
        modifyInLock(configuration -> configuration.addStorage(storage));
    }

    @Override
    public void removeStorage(String storageId)
    {
        modifyInLock(configuration -> configuration.getStorages().remove(storageId));
    }

    @Override
    public void saveRepository(String storageId,
                               MutableRepository repository)
    {
        modifyInLock(configuration ->
                     {
                         final MutableStorage storage = configuration.getStorage(storageId);
                         repository.setStorage(storage);
                         storage.addRepository(repository);

                         if (repository.isEligibleForCustomConnectionPool())
                         {
                             proxyRepositoryConnectionPoolConfigurationService.setMaxPerRepository(
                                     repository.getRemoteRepository().getUrl(),
                                     repository.getHttpConnectionPool().getAllocatedConnections());
                         }
                     });
    }

    @Override
    public void removeRepositoryFromAssociatedGroups(String storageId,
                                                     String repositoryId)
    {
        modifyInLock(configuration ->
                     {
                         List<Repository> includedInGroupRepositories = getConfiguration().getGroupRepositoriesContaining(
                                 storageId, repositoryId);

                         if (!includedInGroupRepositories.isEmpty())
                         {
                             for (Repository repository : includedInGroupRepositories)
                             {
                                 configuration.getStorage(repository.getStorage().getId())
                                              .getRepository(repository.getId())
                                              .getGroupRepositories().remove(repositoryId);
                             }
                         }
                     });
    }

    @Override
    public void removeRepository(String storageId,
                                 String repositoryId)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorage(storageId).removeRepository(repositoryId);
                         removeRepositoryFromAssociatedGroups(storageId, repositoryId);
                     });
    }

    @Override
    public void setProxyRepositoryMaxConnections(String storageId,
                                                 String repositoryId,
                                                 int numberOfConnections)
    {
        modifyInLock(configuration ->
                     {
                         MutableRepository repository = configuration.getStorage(storageId).getRepository(repositoryId);
                         if (repository.getHttpConnectionPool() == null)
                         {
                             repository.setHttpConnectionPool(new MutableHttpConnectionPool());
                         }

                         repository.getHttpConnectionPool().setAllocatedConnections(numberOfConnections);
                     });
    }

    @Override
    public boolean saveAcceptedRuleSet(MutableRuleSet ruleSet)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getRoutingRules().addAcceptRule(ruleSet.getGroupRepository(), ruleSet);
                     });

        return true;
    }

    @Override
    public boolean saveDeniedRuleSet(MutableRuleSet ruleSet)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getRoutingRules().addDenyRule(ruleSet.getGroupRepository(), ruleSet);
                     });

        return true;
    }

    @Override
    public boolean removeAcceptedRuleSet(String groupRepository)
    {
        final MutableBoolean result = new MutableBoolean();
        modifyInLock(configuration ->
                     {
                         final Map<String, MutableRuleSet> accepted = configuration.getRoutingRules().getAccepted();

                         if (accepted.containsKey(groupRepository))
                         {
                             result.setTrue();
                             accepted.remove(groupRepository);
                         }
                     });

        return result.isTrue();
    }

    @Override
    public boolean saveAcceptedRepository(String groupRepository,
                                          MutableRoutingRule routingRule)
    {
        final MutableBoolean result = new MutableBoolean();
        modifyInLock(configuration ->
                     {
                         final Map<String, MutableRuleSet> acceptedRulesMap = configuration.getRoutingRules()
                                                                                           .getAccepted();
                         if (acceptedRulesMap.containsKey(groupRepository))
                         {
                             for (MutableRoutingRule rl : acceptedRulesMap.get(groupRepository).getRoutingRules())
                             {
                                 if (routingRule.getPattern().equals(rl.getPattern()))
                                 {
                                     result.setTrue();
                                     rl.getRepositories().addAll(routingRule.getRepositories());
                                 }
                             }
                         }
                     });

        return result.isTrue();
    }

    @Override
    public boolean removeAcceptedRepository(String groupRepository,
                                            String pattern,
                                            String repositoryId)
    {
        final MutableBoolean result = new MutableBoolean();
        modifyInLock(configuration ->
                     {
                         final Map<String, MutableRuleSet> acceptedRules = configuration.getRoutingRules()
                                                                                        .getAccepted();
                         if (acceptedRules.containsKey(groupRepository))
                         {
                             for (MutableRoutingRule routingRule : acceptedRules.get(groupRepository).getRoutingRules())
                             {
                                 if (pattern.equals(routingRule.getPattern()))
                                 {
                                     result.setTrue();
                                     routingRule.getRepositories().remove(repositoryId);
                                 }
                             }
                         }
                     });

        return result.isTrue();
    }

    @Override
    public boolean overrideAcceptedRepositories(String groupRepository,
                                                MutableRoutingRule routingRule)
    {
        final MutableBoolean result = new MutableBoolean();
        modifyInLock(configuration ->
                     {
                         if (configuration.getRoutingRules().getAccepted().containsKey(groupRepository))
                         {
                             for (MutableRoutingRule rule : configuration.getRoutingRules()
                                                                         .getAccepted()
                                                                         .get(groupRepository)
                                                                         .getRoutingRules())
                             {
                                 if (routingRule.getPattern().equals(rule.getPattern()))
                                 {
                                     result.setTrue();
                                     rule.setRepositories(routingRule.getRepositories());
                                 }
                             }
                         }
                     });

        return result.isTrue();
    }

    @Override
    public void addRepositoryToGroup(String storageId,
                                     String repositoryId,
                                     String repositoryGroupMemberId)
    {
        modifyInLock(configuration ->
                     {
                         final MutableRepository repository = configuration.getStorage(storageId)
                                                                           .getRepository(repositoryId);
                         repository.addRepositoryToGroup(repositoryGroupMemberId);
                     });
    }

    private void setAllows()
    {
        modifyInLock(configuration ->
                     {
                         final Map<String, MutableStorage> storages = configuration.getStorages();

                         if (storages != null && !storages.isEmpty())
                         {
                             for (MutableStorage storage : storages.values())
                             {
                                 if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                                 {
                                     for (MutableRepository repository : storage.getRepositories().values())
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
                     }, false);
    }

    /**
     * Sets the repository <--> storage relationships explicitly, as initially, when these are deserialized from the
     * XML, they have no such relationship.
     */
    private void setRepositoryStorageRelationships()
    {
        modifyInLock(configuration ->
                     {
                         final Map<String, MutableStorage> storages = configuration.getStorages();

                         if (storages != null && !storages.isEmpty())
                         {
                             for (MutableStorage storage : storages.values())
                             {
                                 if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                                 {
                                     for (MutableRepository repository : storage.getRepositories().values())
                                     {
                                         repository.setStorage(storage);
                                     }
                                 }
                             }
                         }
                     }, false);
    }

    @Override
    public void setRepositoryArtifactCoordinateValidators()
    {
        modifyInLock(configuration ->
                     {
                         final Map<String, MutableStorage> storages = configuration.getStorages();

                         if (storages != null && !storages.isEmpty())
                         {
                             for (MutableStorage storage : storages.values())
                             {
                                 if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                                 {
                                     for (MutableRepository repository : storage.getRepositories().values())
                                     {
                                         LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(
                                                 repository.getLayout());

                                         // Generally, this should not happen. However, there are at least two cases where it may occur:
                                         // 1) During testing -- various modules are not added as dependencies and a layout provider
                                         //    is thus not registered.
                                         // 2) Syntax error, or some other mistake leading to an incorrectly defined layout
                                         //    for a repository.
                                         if (layoutProvider != null)
                                         {
                                             @SuppressWarnings("unchecked")
                                             Set<String> defaultArtifactCoordinateValidators = layoutProvider.getDefaultArtifactCoordinateValidators();
                                             if ((repository.getArtifactCoordinateValidators() == null ||
                                                  (repository.getArtifactCoordinateValidators() != null &&
                                                   repository.getArtifactCoordinateValidators().isEmpty())) &&
                                                 defaultArtifactCoordinateValidators != null)
                                             {
                                                 repository.setArtifactCoordinateValidators(
                                                         defaultArtifactCoordinateValidators);
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     });
    }

    @Override
    public void putInService(final String storageId,
                             final String repositoryId)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorage(storageId)
                                      .getRepository(repositoryId)
                                      .setStatus(RepositoryStatusEnum.IN_SERVICE.getStatus());

                         RepositoryEvent event = new RepositoryEvent(storageId,
                                                                     repositoryId,
                                                                     RepositoryEventTypeEnum.EVENT_REPOSITORY_PUT_IN_SERVICE
                                                                             .getType());

                         repositoryEventListenerRegistry.dispatchEvent(event);
                     });
    }

    @Override
    public void putOutOfService(final String storageId,
                                final String repositoryId)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorage(storageId)
                                      .getRepository(repositoryId)
                                      .setStatus(RepositoryStatusEnum.OUT_OF_SERVICE.getStatus());

                         RepositoryEvent event = new RepositoryEvent(storageId,
                                                                     repositoryId,
                                                                     RepositoryEventTypeEnum.EVENT_REPOSITORY_PUT_OUT_OF_SERVICE
                                                                             .getType());

                         repositoryEventListenerRegistry.dispatchEvent(event);
                     });
    }

    @Override
    public void setArtifactMaxSize(final String storageId,
                                   final String repositoryId,
                                   final long value)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorage(storageId)
                                      .getRepository(repositoryId)
                                      .setArtifactMaxSize(value);
                     });
    }

    @Override
    public void set(final MutableRemoteRepositoryRetryArtifactDownloadConfiguration remoteRepositoryRetryArtifactDownloadConfiguration)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getRemoteRepositoriesConfiguration()
                                      .setRemoteRepositoryRetryArtifactDownloadConfiguration(
                                              remoteRepositoryRetryArtifactDownloadConfiguration);
                     });
    }

    @Override
    public void addRepositoryArtifactCoordinateValidator(final String storageId,
                                                         final String repositoryId,
                                                         final String alias)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorage(storageId).getRepository(
                                 repositoryId).getArtifactCoordinateValidators().put(alias, alias);
                     });
    }

    @Override
    public boolean removeRepositoryArtifactCoordinateValidator(final String storageId,
                                                               final String repositoryId,
                                                               final String alias)
    {
        final MutableBoolean result = new MutableBoolean();
        modifyInLock(configuration ->
                     {
                         result.setValue(configuration.getStorage(storageId).getRepository(
                                 repositoryId).getArtifactCoordinateValidators().remove(alias, alias));
                     });

        return result.isTrue();
    }

    @Override
    public void setCorsAllowedOrigins(final List<String> allowedOrigins)
    {
        modifyInLock(configuration ->
                     {
                         ArrayList origins;

                         if(CollectionUtils.isEmpty(allowedOrigins))
                         {
                             origins = new ArrayList<>();
                         } else
                         {
                             origins = new ArrayList<>(allowedOrigins);
                         }

                         configuration.getCorsConfiguration()
                                      .setAllowedOrigins(origins);
                     });
    }

    private void setProxyRepositoryConnectionPoolConfigurations()
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorages().values().stream()
                                      .filter(storage -> MapUtils.isNotEmpty(storage.getRepositories()))
                                      .flatMap(storage -> storage.getRepositories().values().stream())
                                      .filter(MutableRepository::isEligibleForCustomConnectionPool)
                                      .forEach(
                                              repository -> proxyRepositoryConnectionPoolConfigurationService.setMaxPerRepository(
                                                      repository.getRemoteRepository().getUrl(),
                                                      repository.getHttpConnectionPool().getAllocatedConnections()));
                     }, false);
    }

    @Override
    public void setSmtpSettings(MutableSmtpConfiguration smtpConfiguration)
    {
        modifyInLock(configuration -> {
            configuration.getSmtpConfiguration().setHost(smtpConfiguration.getHost());
            configuration.getSmtpConfiguration().setPort(smtpConfiguration.getPort());
            configuration.getSmtpConfiguration().setConnection(smtpConfiguration.getConnection());
            configuration.getSmtpConfiguration().setUsername(smtpConfiguration.getUsername());
            configuration.getSmtpConfiguration().setPassword(smtpConfiguration.getPassword());
        });
    }

    private void modifyInLock(final Consumer<MutableConfiguration> operation)
    {
        modifyInLock(operation, true);
    }

    private void modifyInLock(final Consumer<MutableConfiguration> operation,
                              final boolean storeInFile)
    {
        final Lock writeLock = configurationLock.writeLock();
        writeLock.lock();

        try
        {
            operation.accept(configuration);

            if (storeInFile)
            {
                configurationFileManager.store(configuration);
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

}
