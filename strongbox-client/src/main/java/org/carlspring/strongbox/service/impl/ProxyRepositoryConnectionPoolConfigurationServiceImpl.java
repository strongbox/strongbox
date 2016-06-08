package org.carlspring.strongbox.service.impl;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.carlspring.strongbox.data.domain.PoolConfiguration;
import org.carlspring.strongbox.data.service.PoolConfigurationService;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author korest
 */
@Component
public class ProxyRepositoryConnectionPoolConfigurationServiceImpl
        implements ProxyRepositoryConnectionPoolConfigurationService
{

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ProxyRepositoryConnectionPoolConfigurationServiceImpl.class);

    private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;
    private IdleConnectionMonitorThread idleConnectionMonitorThread;

    @Value("${pool.maxConnections:200}")
    private Integer maxTotal;
    @Value("${pool.defaultConnectionsPerRoute:5}")
    private Integer defaultMaxPerRoute;
    @Autowired
    private PoolConfigurationService poolConfigurationService;

    @PostConstruct
    public void init()
    {
        poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setMaxTotal(maxTotal); //TODO  alue that depends on number of threads?
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);

        // set all repositories configurations on start
        Optional<Iterable<PoolConfiguration>> poolConfigurationsOptional = poolConfigurationService.findAll();
        poolConfigurationsOptional.ifPresent(poolConfigurations -> {
            for(PoolConfiguration poolConfiguration : poolConfigurations)
            {
                HttpRoute httpRoute = getHttpRouteFromRepository(poolConfiguration.getRepositoryUrl());
                poolingHttpClientConnectionManager.setMaxPerRoute(httpRoute, poolConfiguration.getMaxConnections());
            }
        });

        // thread for monitoring unused connections
        idleConnectionMonitorThread = new IdleConnectionMonitorThread(poolingHttpClientConnectionManager);
        idleConnectionMonitorThread.setDaemon(true);
        idleConnectionMonitorThread.start();
    }

    @PreDestroy
    public void destroy()
    {
        shutdown();
    }

    public Client getClient()
    {
        ClientConfig config = new ClientConfig();
        config.connectorProvider(new ApacheConnectorProvider());
        config.property(ApacheClientProperties.CONNECTION_MANAGER, poolingHttpClientConnectionManager);
        // property to prevent closing connection manager when client is closed
        config.property(ApacheClientProperties.CONNECTION_MANAGER_SHARED, true);

        // TODO set basic authentication here instead of setting it always in client?
        /* CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
        config.property(ApacheClientProperties.CREDENTIALS_PROVIDER, credentialsProvider); */

        return ClientBuilder.newBuilder().newClient(config);
    }

    @Override
    public void setMaxTotal(int max)
    {
        poolingHttpClientConnectionManager.setMaxTotal(max);
    }

    @Override
    public void setDefaultMaxPerRepository(int defaultMax)
    {
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(defaultMax);
    }

    @Override
    public int getDefaultMaxPerRepository()
    {
        return poolingHttpClientConnectionManager.getDefaultMaxPerRoute();
    }

    @Override
    public void setMaxPerRepository(String repository, int max)
    {
        HttpRoute httpRoute = getHttpRouteFromRepository(repository);
        poolConfigurationService.createOrUpdateNumberOfConnectionsForRepository(repository, max);
        poolingHttpClientConnectionManager.setMaxPerRoute(httpRoute, max);
    }

    @Override
    public PoolStats getTotalStats()
    {
        return poolingHttpClientConnectionManager.getTotalStats();
    }

    @Override
    public PoolStats getPoolStats(String repository)
    {
        HttpRoute httpRoute = getHttpRouteFromRepository(repository);
        return poolingHttpClientConnectionManager.getStats(httpRoute);
    }

    @Override
    public void shutdown()
    {
        idleConnectionMonitorThread.shutdown();
        poolingHttpClientConnectionManager.shutdown();
    }

    // code to create HttpRoute the same as in apache library
    private HttpRoute getHttpRouteFromRepository(String repository)
    {
        try
        {
            URI uri = new URI(repository);
            boolean secure = uri.getScheme().equalsIgnoreCase("https");
            int port = uri.getPort();
            if (uri.getPort() > 0)
            {
                port = uri.getPort();
            }
            else if (uri.getScheme().equalsIgnoreCase("https"))
            {
                port = 443;
            }
            else if (uri.getScheme().equalsIgnoreCase("http"))
            {
                port = 80;
            }
            else
            {
                LOGGER.warn("Unknown port of uri %s", repository);
            }

            HttpHost httpHost = new HttpHost(uri.getHost(), port, uri.getScheme());
            // TODO check whether we need second param InetAddress
            return new HttpRoute(httpHost, null, secure);
        }
        catch (URISyntaxException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        // default http route creation
        return new HttpRoute(HttpHost.create(repository));
    }

    private static final class IdleConnectionMonitorThread extends Thread
    {

        private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;

        private volatile boolean shutdown;

        IdleConnectionMonitorThread(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager)
        {
            super();
            this.poolingHttpClientConnectionManager = poolingHttpClientConnectionManager;
        }

        @Override
        public void run()
        {
            try
            {
                while (!shutdown)
                {
                    synchronized (this)
                    {
                        wait(5000);
                        poolingHttpClientConnectionManager.closeExpiredConnections();
                        poolingHttpClientConnectionManager.closeIdleConnections(60, TimeUnit.SECONDS);
                    }
                }
            }
            catch (InterruptedException e)
            {
                shutdown();
            }
        }

        public void shutdown()
        {
            shutdown = true;
            synchronized (this)
            {
                notifyAll();
            }
        }

    }
}
