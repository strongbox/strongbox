package org.carlspring.strongbox.services;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.HttpConnectionPool;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;

import java.util.List;

/**
 * @author mtodorov
 */
public interface ConfigurationManagementService
{

    Configuration getConfiguration();

    void setConfiguration(Configuration configuration);

    void save(Configuration Configuration);

    String getInstanceName();

    void setInstanceName(String instanceName);

    String getBaseUrl();

    void setBaseUrl(String baseUrl);

    int getPort();

    void setPort(int port);

    void setProxyConfiguration(String storageId,
                               String repositoryId,
                               ProxyConfiguration proxyConfiguration);

    void saveStorage(Storage storage);

    Storage getStorage(String storageId);

    void removeStorage(String storageId);

    void saveRepository(String storageId,
                        Repository repository);

    Repository getRepository(String storageId,
                             String repositoryId);

    List<Repository> getRepositoriesWithLayout(String storageId,
                                               String layout);

    List<Repository> getGroupRepositories();

    List<Repository> getGroupRepositoriesContaining(String storageId,
                                                    String repositoryId);

    void removeRepositoryFromAssociatedGroups(String storageId,
                                              String repositoryId);

    void removeRepository(String storageId,
                          String repositoryId);

    ProxyConfiguration getProxyConfiguration();

    void setProxyRepositoryMaxConnections(String storageId,
                                          String repositoryId,
                                          int numberOfConnections);

    HttpConnectionPool getHttpConnectionPoolConfiguration(String storageId,
                                                          String repositoryId);

    boolean saveAcceptedRuleSet(RuleSet ruleSet);

    boolean saveDeniedRuleSet(RuleSet ruleSet);

    boolean removeAcceptedRuleSet(String groupRepository);

    boolean saveAcceptedRepository(String groupRepository,
                                   RoutingRule routingRule);

    boolean removeAcceptedRepository(String groupRepository,
                                     String pattern,
                                     String repositoryId);

    boolean overrideAcceptedRepositories(String groupRepository,
                                         RoutingRule routingRule);

    RoutingRules getRoutingRules();
}
