package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.config.WebConfig;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;

/**
 * @author Alex Oreshkevich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebConfig.class })
@Commit
public class JerseyExampleTest
        extends JerseyTest
{

    public final static String ADMIN_CREDENTIALS = "Basic " + new String(Base64.encode("admin:password".getBytes()));

    protected Invocation.Builder requestApi(String path)
    {
        return target(path).request().header(HttpHeaders.AUTHORIZATION, ADMIN_CREDENTIALS);
    }

    @Override
    protected TestContainerFactory getTestContainerFactory()
    {
        // use Grizzly 2.0: HttpServer; do not require jetty:run, start jetty in blocked mode or something like that
        // makes possible to execute unit tests from IDE without separate HTTP server settings
        return new GrizzlyTestContainerFactory();
    }

    @Override
    public Application configure()
    {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        forceSet(TestProperties.CONTAINER_PORT, "0"); // execute test on first available port

        ResourceConfig rc = new ResourceConfig(UserRestlet.class);

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(WebConfig.class);
        rc.property("contextConfig", ctx);

        return rc;
    }


    @Override
    protected void configureClient(ClientConfig config)
    {
        // example of configuring client request filters
        // here we require that HTTP Basic authorization should be present in any case
        config.register(new ClientRequestFilter()
        {
            @Override
            public void filter(ClientRequestContext requestContext)
                    throws IOException
            {
                if (requestContext.getHeaders().get(HttpHeaders.AUTHORIZATION) == null)
                {
                    requestContext.abortWith(
                            Response.status(Response.Status.UNAUTHORIZED)
                                    .entity("Requests should be authorized.")
                                    .build());
                }
            }
        });
        super.configureClient(config);
    }

    // simplest possible test; just to make sure that unit test configuration is correct
    // didn't call any rest api endpoint
    @Test
    public void simplestTest()
    {
        System.out.println("OK");
    }

    // test secured rest api endpoint
    @Test
    public void greetingServerTest()
            throws Exception
    {
        final String message = requestApi("/users/greet").get(String.class);
        assertEquals("\"hello, greet\"", message);
    }
}
