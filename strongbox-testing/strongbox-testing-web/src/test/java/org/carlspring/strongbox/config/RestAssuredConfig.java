package org.carlspring.strongbox.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestAssuredConfig
{

    public final static String DEFAULT_PORT = "48080";

    public final static String DEFAULT_HOST = "localhost";

    @Bean
    public URI contextBaseUrl()
    {
        // initialize host
        String host = System.getProperty("strongbox.host", DEFAULT_HOST);
        String strongboxPort = System.getProperty("strongbox.port", DEFAULT_PORT);

        return URI.create("http://" + host + ":" + strongboxPort);
    }
    
}
