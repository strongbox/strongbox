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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


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
            logger.debug("Proxy configuration settings for Repository [{}] are {}", repository.getId(),
                         repositoryProxyConfiguration);

            if (isValidProxyConfiguration(repositoryProxyConfiguration))
            {
                return proxyRepositoryConnectionPoolConfigurationService.getRestClient(repositoryProxyConfiguration);
            }

            ProxyServerConfiguration globalProxyConfiguration = getGlobalProxyConfiguration();

            if (isValidProxyConfiguration(globalProxyConfiguration))
            {
                return proxyRepositoryConnectionPoolConfigurationService.getRestClient(globalProxyConfiguration);
            }
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            logger.error("Exception occured while creating client with proxy configurations.", e);

        }

        return proxyRepositoryConnectionPoolConfigurationService.getRestClient();
    }


    @Override
    public Client getClientForRepository(RemoteRepository remoteRepository)
    {
        try
        {
            ProxyServerConfiguration globalProxyConfiguration = getGlobalProxyConfiguration();
            if (isValidProxyConfiguration(globalProxyConfiguration))
            {
                return proxyRepositoryConnectionPoolConfigurationService.getRestClient(globalProxyConfiguration);
            }

        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            logger.error("Exception occured while creating client with proxy configurations.", e);
        }

        return proxyRepositoryConnectionPoolConfigurationService.getRestClient();
    }

    private ProxyServerConfiguration getGlobalProxyConfiguration()
        throws IllegalAccessException,
        InvocationTargetException
    {
        ProxyConfiguration proxyConfiguration = configurationManagementService.getConfiguration()
                                                                              .getProxyConfiguration();

        if (proxyConfiguration == null)
        {
            return null;
        }

        ProxyServerConfiguration globalProxyConfiguration = new ProxyServerConfiguration();
        BeanUtils.copyProperties(globalProxyConfiguration, proxyConfiguration);

        logger.debug("Global Proxy configuration settings are {}", globalProxyConfiguration);
        return globalProxyConfiguration;

    }

    @Override
    public CloseableHttpClient getHttpClient()
    {
        ProxyServerConfiguration globalProxyConfiguration = null;
        try
        {
            globalProxyConfiguration = getGlobalProxyConfiguration();

        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            logger.error("Something went wrong while creating http client with proxy configurations.", e);
        }

        return proxyRepositoryConnectionPoolConfigurationService.getHttpClient(globalProxyConfiguration);
    }

    @Override
    public CloseableHttpClient getHttpClient(Repository repository)
    {
        try
        {
            ProxyServerConfiguration repositoryProxyConfiguration = repository.getProxyServerConfiguration();
            logger.debug("Proxy configuration settings for Repository [{}] are {}", repository.getId(),
                         repositoryProxyConfiguration);

            if (isValidProxyConfiguration(repositoryProxyConfiguration))
            {
                return proxyRepositoryConnectionPoolConfigurationService.getHttpClient(repositoryProxyConfiguration);
            }

        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            logger.error("Something went wrong while creating http client with proxy configurations.", e);
        }

        return getHttpClient();
    }

    private boolean isValidProxyConfiguration(ProxyServerConfiguration repositoryProxyConfiguration)
    {
        return (repositoryProxyConfiguration != null 
                && repositoryProxyConfiguration.getPort() != null
                && !StringUtils.isEmpty(repositoryProxyConfiguration.getHost())
                && !StringUtils.isEmpty(repositoryProxyConfiguration.getType()));
    }
}
