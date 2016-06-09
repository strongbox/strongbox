package org.carlspring.strongbox.client;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author mtodorov
 */
@Ignore
public class HttpArtifactClientFactoryTest
{


    public static final String HOST_NAME = "localhost";

    public static final String USERNAME = "user1";

    public static final String PASSWORD = "password";

    private CloseableHttpClient client;


    @Before
    public void setUp()
            throws Exception
    {
        client = HttpArtifactClientFactory.createHttpClientWithAuthentication(HOST_NAME, USERNAME, PASSWORD);
    }

    @Test
    public void testCreateHttpClientWithAuthentication()
            throws IOException
    {
        // TODO: 1) Attempt to connect.
        HttpHost target = new HttpHost("localhost", 80, "http");
        retrieveUrl(target, null, client);

        // TODO: 2) Assert there was no error.
    }


    @Test
    public void testCreateHttpClientWithAuthenticatedProxy()
            throws IOException
    {
        // TODO: 1) Attempt to connect.
        HttpHost target = new HttpHost("localhost", 80, "http");
        retrieveUrl(target, null, client);

        // TODO: 2) Assert there was no error.
    }

    protected void retrieveUrl(HttpHost target,
                               HttpHost proxy,
                               CloseableHttpClient client)
            throws IOException
    {
        try
        {
            RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
            HttpGet request = new HttpGet("/");
            request.setConfig(config);

            if (proxy != null)
            {
                System.out.println("Executing request " + request.getRequestLine() + " to " + target + " via " + proxy + "...");
            }
            else
            {
                System.out.println("Executing request " + request.getRequestLine() + " to " + target + "...");
            }

            CloseableHttpResponse response = client.execute(target, request);
            try
            {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());

                EntityUtils.consume(response.getEntity());
            }
            finally
            {
                response.close();
            }

            assertEquals(200, response.getStatusLine().getStatusCode());
        }
        finally
        {
            client.close();
        }
    }

}
