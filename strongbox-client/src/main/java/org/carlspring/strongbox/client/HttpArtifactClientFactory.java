package org.carlspring.strongbox.client;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;

/**
 * @author carlspring
 */
public class HttpArtifactClientFactory
{

    private HttpArtifactClientFactory() 
    {
    }

    public static CloseableHttpClient createHttpClientWithAuthentication(String hostName,
                                                                         String username,
                                                                         String password)
    {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(hostName, AuthScope.ANY_PORT), credentials);

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.useSystemProperties();
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());

        return clientBuilder.build();
    }

    public static CloseableHttpClient createHttpClientWithAuthenticatedProxy(String username,
                                                                             String password,
                                                                             HttpHost proxy)
    {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(proxy.getHostName(), AuthScope.ANY_PORT), credentials);

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.useSystemProperties();
        clientBuilder.setProxy(proxy);
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());

        return clientBuilder.build();
    }

    public static HttpHost createProxy(String hostname, int port, String protocol)
    {
        return new HttpHost(hostname, port, protocol);
    }

}
