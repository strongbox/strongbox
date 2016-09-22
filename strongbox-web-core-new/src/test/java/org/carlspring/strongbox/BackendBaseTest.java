package org.carlspring.strongbox;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Alex Oreshkevich
 */
public abstract class BackendBaseTest {

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

    public String getContextBaseUrl() {
        if (contextBaseUrl == null) {
            contextBaseUrl = protocol + "://" + host + ":" + port;
        }

        return contextBaseUrl;
    }


}