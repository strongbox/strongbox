package org.carlspring.strongbox.service.impl;

import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.http.ssl.SSLContexts;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author korest
 */
@Component
public class ProxyRepositoryConnectionPoolConfigurationServiceImpl
        implements ProxyRepositoryConnectionPoolConfigurationService
{

    private static final Logger LOGGER = LoggerFactory.getLogger(
            ProxyRepositoryConnectionPoolConfigurationServiceImpl.class);

    private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;

    private IdleConnectionMonitorThread idleConnectionMonitorThread;

    @Value("${pool.maxConnections:200}")
    private int maxTotal;

    @Value("${pool.defaultConnectionsPerRoute:5}")
    private int defaultMaxPerRoute;

    @Value("${pool.idleConnectionsTimeoutInSeconds:60}")
    private int idleConnectionsTimeoutInSeconds;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${server.ssl.key-store-type:PKCS12}")
    private String keyStoreType;

    @Value("${server.ssl.key-store-password:password}")
    private String keyStorePassword;

    @Value("${server.ssl.key-store:keystore.p12}")
    private FileSystemResource keyStoreResource;

    @Value("${server.ssl.trust-store-password:password}")
    private String trustStorePassword;

    @Value("${server.ssl.trust-store:truststore.p12}")
    private FileSystemResource trustStoreResource;

    @PostConstruct
    public void init()
            throws IOException, GeneralSecurityException
    {
        if (sslEnabled)
        {
            Registry<ConnectionSocketFactory> socketFactoryRegistry = getConnectionSocketFactoryRegistry();
            poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        }
        else
        {
            poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        }

        poolingHttpClientConnectionManager.setMaxTotal(maxTotal); //TODO value that depends on number of threads?
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);

        // thread for monitoring unused connections
        idleConnectionMonitorThread =
                new IdleConnectionMonitorThread(poolingHttpClientConnectionManager, idleConnectionsTimeoutInSeconds);
        idleConnectionMonitorThread.setDaemon(true);
        idleConnectionMonitorThread.start();
    }


    @PreDestroy
    public void destroy()
    {
        shutdown();
    }

    @Override
    public Client getRestClient()
    {
        ClientConfig config = new ClientConfig();
        config.connectorProvider(new ApacheConnectorProvider());
        config.property(ApacheClientProperties.CONNECTION_MANAGER, poolingHttpClientConnectionManager);
        // property to prevent closing connection manager when client is closed
        config.property(ApacheClientProperties.CONNECTION_MANAGER_SHARED, true);

        java.util.logging.Logger logger = java.util.logging.Logger.getLogger("org.carlspring.strongbox.RestClient");

        // TODO set basic authentication here instead of setting it always in client?
        /* CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        config.property(ApacheClientProperties.CREDENTIALS_PROVIDER, credentialsProvider); */

        return ClientBuilder.newBuilder()
                            .register(new LoggingFeature(logger, Verbosity.PAYLOAD_TEXT))
                            .withConfig(config)
                            .build();
    }

    @Override
    public CloseableHttpClient getHttpClient()
    {
        return HttpClients.custom()
                          .setConnectionManagerShared(true)
                          .setConnectionManager(poolingHttpClientConnectionManager)
                          .build();
    }

    @Override
    public void setMaxTotal(int max)
    {
        poolingHttpClientConnectionManager.setMaxTotal(max);
    }

    @Override
    public int getDefaultMaxPerRepository()
    {
        return poolingHttpClientConnectionManager.getDefaultMaxPerRoute();
    }

    @Override
    public void setDefaultMaxPerRepository(int defaultMax)
    {
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(defaultMax);
    }

    @Override
    public void setMaxPerRepository(String repository,
                                    int max)
    {
        if (max > 0)
        {
            HttpRoute httpRoute = getHttpRouteFromRepository(repository);
            poolingHttpClientConnectionManager.setMaxPerRoute(httpRoute, max);
        }
        else
        {
            LOGGER.warn("Not setting max repository connections to %s as it is no positive value", max);
        }
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

    private Registry<ConnectionSocketFactory> getConnectionSocketFactoryRegistry()
            throws IOException, GeneralSecurityException
    {
        SSLContext sslContext = SSLContexts
                                        .custom()
                                        .loadKeyMaterial(loadKeyStore(keyStoreType, keyStoreResource,
                                                                      keyStorePassword.toCharArray()),
                                                         keyStorePassword.toCharArray())
                                        .loadTrustMaterial(trustStoreResource.getFile(),
                                                           trustStorePassword.toCharArray())
                                        .build();

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);
        return RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslConnectionSocketFactory)
                                                                .build();
    }

    private KeyStore loadKeyStore(String keyStoreType,
                                  Resource keyStoreResource,
                                  char[] keyStorePassword)
            throws IOException, GeneralSecurityException
    {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        try (final InputStream inputStream = new BufferedInputStream(keyStoreResource.getInputStream()))
        {
            keyStore.load(inputStream, keyStorePassword);
        }
        return keyStore;
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

    private static final class IdleConnectionMonitorThread
            extends Thread
    {

        private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;

        private volatile boolean shutdown;

        private int idleConnectionsTimeout;

        IdleConnectionMonitorThread(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager,
                                    int idleConnectionsTimeout)
        {
            super();
            this.poolingHttpClientConnectionManager = poolingHttpClientConnectionManager;
            this.idleConnectionsTimeout = idleConnectionsTimeout;
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
                        poolingHttpClientConnectionManager.closeIdleConnections(idleConnectionsTimeout,
                                                                                TimeUnit.SECONDS);
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
