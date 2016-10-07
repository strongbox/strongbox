package org.carlspring.strongbox.rest;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

/**
 * General settings for the testing subsystem.
 *
 * @author Alex Oreshkevich
 */
public abstract class BackendBaseTest
{

    public final static int DEFAULT_PORT = 48080;

    /**
     * Share logger instance across all tests.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    protected WebApplicationContext context;

    private String host;

    private int port;

    private String contextBaseUrl;

    public BackendBaseTest()
    {

        // initialize host
        host = System.getProperty("strongbox.host");
        if (host == null)
        {
            host = "localhost";
        }

        // initialize port
        String strongboxPort = System.getProperty("strongbox.port");
        if (strongboxPort == null)
        {
            port = DEFAULT_PORT;
        }
        else
        {
            port = Integer.parseInt(strongboxPort);
        }

        // initialize base URL
        contextBaseUrl = "http://" + host + ":" + port;
    }

    @Before
    public void init()
    {
        RestAssuredMockMvc.webAppContextSetup(context);
    }

    @After
    public void shutdown()
    {
        RestAssuredMockMvc.reset();
    }

    public String getContextBaseUrl()
    {
        return contextBaseUrl;
    }
}