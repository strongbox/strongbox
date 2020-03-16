package org.carlspring.strongbox.client.config;

import org.carlspring.strongbox.client.ProxyServerConfiguration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author ankit.tomar
 */
@Component
public class ProxyRepositoryConnectionConfigurationServiceImpl implements ProxyRepositoryConnectionConfigurationService
{

    private static final Logger logger = LoggerFactory.getLogger(ProxyRepositoryConnectionConfigurationServiceImpl.class);

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    public Client getClientForRepository(Repository repository)
    {

        try
        {
            ProxyServerConfiguration repositoryProxyConfiguration = repository.getProxyServerConfiguration();
            logger.debug("Proxy configuration settings for Repository [{}] are {}", repository.getId(), repositoryProxyConfiguration);

            if (repositoryProxyConfiguration != null)
            {
                return proxyRepositoryConnectionPoolConfigurationService.getRestClient(repositoryProxyConfiguration);
            }

            ProxyServerConfiguration globalProxyConfiguration = getGlobalProxyConfiguration();

            if (globalProxyConfiguration != null)
            {
                return proxyRepositoryConnectionPoolConfigurationService.getRestClient(globalProxyConfiguration);
            }

            return proxyRepositoryConnectionPoolConfigurationService.getRestClient();
        }
        catch (IllegalAccessException | InvocationTargetException | MalformedURLException e)
        {
            logger.error("Exception occured while creating client with proxy configurations.", e);

        }
        return null;
    }

    @Override
    public Client getClientForRepository(RemoteRepository remoteRepository)
    {
        ProxyServerConfiguration globalProxyConfiguration = getGlobalProxyConfiguration();
        try
        {
            if (globalProxyConfiguration != null)
            {
                return proxyRepositoryConnectionPoolConfigurationService.getRestClient(globalProxyConfiguration);
            }
            return proxyRepositoryConnectionPoolConfigurationService.getRestClient();
        }
        catch (MalformedURLException e)
        {
            logger.error("Exception occured while creating client with proxy configurations.", e);

        }

        return null;
    }

    private ProxyServerConfiguration getGlobalProxyConfiguration()
    {
        ProxyConfiguration proxyConfiguration = configurationManagementService.getConfiguration()
                                                                              .getProxyConfiguration();

        if (proxyConfiguration == null)
        {
            return null;
        }

        ProxyServerConfiguration globalProxyConfiguration = new ProxyServerConfiguration();
        try
        {
            BeanUtils.copyProperties(globalProxyConfiguration, proxyConfiguration);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            logger.error("Something went wrong while copying proxy properties.", e);
        }

        logger.debug("Global Proxy configuration settings are {}", globalProxyConfiguration);
        return globalProxyConfiguration;
    }
}
