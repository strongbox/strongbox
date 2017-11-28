package org.carlspring.strongbox.config;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

@Configuration
public class RestAssuredConfig
{

    public final static String DEFAULT_PORT = "48080";

    public final static String DEFAULT_HOST = "localhost";

    @Inject
    protected WebApplicationContext context;

    @PostConstruct
    public void init()
        throws Exception
    {
        RestAssuredMockMvc.webAppContextSetup(context);
    }

    @Bean
    public String contextBaseUrl()
    {
        // initialize host
        String host = System.getProperty("strongbox.host", DEFAULT_HOST);
        String strongboxPort = System.getProperty("strongbox.port", DEFAULT_PORT);

        return "http://" + host + ":" + strongboxPort;

    }
}
