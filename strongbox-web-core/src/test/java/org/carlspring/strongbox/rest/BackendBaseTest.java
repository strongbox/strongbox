package org.carlspring.strongbox.rest;

/*import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;*/

import java.util.logging.Logger;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

/*import org.glassfish.jersey.internal.ServiceFinderBinder;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;*/

/**
 * @author Alex Oreshkevich
 */

public abstract class BackendBaseTest
{

    private static final Logger LOGGER = Logger.getLogger(RestAssuredMockMvc.class.getName());

    @Autowired
    protected WebApplicationContext context;
    private String protocol = "http";
    private String host =
            System.getProperty("strongbox.host") != null ? System.getProperty("strongbox.host") : "localhost";
    private int port = System.getProperty("strongbox.port") != null ?
                       Integer.parseInt(System.getProperty("strongbox.port")) :
                       48080;
    private String contextBaseUrl;

    @Before
    public void init() {
        RestAssuredMockMvc.webAppContextSetup(context);
    }

    @After
    public void shutdown() {
        RestAssuredMockMvc.reset();
    }

    public String getContextBaseUrl()
    {
        if (contextBaseUrl == null)
        {
            contextBaseUrl = protocol + "://" + host + ":" + port;
        }

        return contextBaseUrl;
    }


}
