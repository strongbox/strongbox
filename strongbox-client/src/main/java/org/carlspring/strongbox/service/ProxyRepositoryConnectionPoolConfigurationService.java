package org.carlspring.strongbox.service;

import org.carlspring.strongbox.client.ProxyServerConfiguration;

import javax.ws.rs.client.Client;

import java.net.MalformedURLException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.pool.PoolStats;

/**
 * @author korest
 */
public interface ProxyRepositoryConnectionPoolConfigurationService
{

    Client getRestClient()
        throws MalformedURLException;

    CloseableHttpClient getHttpClient();

    void setMaxTotal(int max);

    void setDefaultMaxPerRepository(int defaultMax);

    int getDefaultMaxPerRepository();

    void setMaxPerRepository(String repository,
                             int max);

    PoolStats getTotalStats();

    PoolStats getPoolStats(String repository);

    void shutdown();

    Client getRestClient(ProxyServerConfiguration proxyConfiguration)
        throws MalformedURLException;
}
