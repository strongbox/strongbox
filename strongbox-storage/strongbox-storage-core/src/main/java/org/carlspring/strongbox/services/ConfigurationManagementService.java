package org.carlspring.strongbox.services;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.HttpConnectionPool;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;

/**
 * @author mtodorov
 */
public interface ConfigurationManagementService extends ConfigurationService
{

    void setConfiguration(Configuration configuration)
            throws IOException, JAXBException;

    String getBaseUrl()
            throws IOException;

    void setBaseUrl(String baseUrl)
            throws IOException, JAXBException;

    int getPort()
            throws IOException;

    void setPort(int port)
            throws IOException, JAXBException;

    void setProxyConfiguration(String storageId,
                               String repositoryId,
                               ProxyConfiguration proxyConfiguration)
            throws IOException, JAXBException;

    void saveStorage(Storage storage)
            throws IOException, JAXBException;

    Storage getStorage(String storageId)
            throws IOException;

    void removeStorage(String storageId)
            throws IOException, JAXBException;

    void saveRepository(String storageId,
                        Repository repository)
            throws IOException, JAXBException;

    Repository getRepository(String storageId, String repositoryId)
            throws IOException;

    List<Repository> getRepositoriesWithLayout(String storageId,
                                               String layout);

    List<Repository> getGroupRepositories();

    List<Repository> getGroupRepositoriesContaining(String storageId,
                                                    String repositoryId);

    void removeRepositoryFromAssociatedGroups(String storageId,
                                              String repositoryId)
            throws IOException, JAXBException;

    void removeRepository(String storageId,
                          String repositoryId)
            throws IOException, JAXBException;

    ProxyConfiguration getProxyConfiguration()
            throws IOException, JAXBException;

    void setProxyRepositoryMaxConnections(String storageId, String repositoryId, int numberOfConnections)
            throws IOException, JAXBException;

    HttpConnectionPool getHttpConnectionPoolConfiguration(String storageId, String repositoryId)
            throws IOException, JAXBException;

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
