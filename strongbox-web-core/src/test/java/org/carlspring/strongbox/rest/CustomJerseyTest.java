package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.config.WebConfig;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.util.Base64;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Base class for all Jersey tests for the Strongbox application.
 *
 * @author Alex Oreshkevich
 */
public abstract class CustomJerseyTest
        extends JerseyTest
{

    // make sure that credentials match to the one that used in user configuration
    // (default) /strongbox-user-management/src/main/resources/etc/conf/security-users.xml
    public final static String ADMIN_CREDENTIALS = "Basic " + new String(Base64.encode("admin:password".getBytes()));

    public final static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Specifies usage of grizzly 2 http server and forbid overriding in any particular test.
     */
    @Override
    protected final TestContainerFactory getTestContainerFactory()
    {
        // use Grizzly 2.0: HttpServer; do not require jetty:run, start jetty in blocked mode or something like that
        // makes possible to execute unit tests from IDE without separate HTTP server settings
        return new GrizzlyTestContainerFactory();
    }

    @Override
    public final Application configure()
    {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        forceSet(TestProperties.CONTAINER_PORT, "48080"); // execute test on first available port

        // refer to package with all restlets; scan packages recursively
        ResourceConfig rc = new ResourceConfig().packages(true, "org.carlspring.strongbox.rest");
        rc.property("contextConfig", new AnnotationConfigApplicationContext(WebConfig.class));
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

    /**
     * Helper method for calling any secured REST API endpoint.
     *
     * @param path REST API endpoint
     * @return request builder
     */
    @SuppressWarnings("unused")
    protected Invocation.Builder requestApi(String path)
    {
        return target(path).request().header(HttpHeaders.AUTHORIZATION, ADMIN_CREDENTIALS);
    }
}
