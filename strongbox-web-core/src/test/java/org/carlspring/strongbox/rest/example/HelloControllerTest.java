package org.carlspring.strongbox.rest.example;

import org.carlspring.strongbox.config.WebConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

/**
 * Example of accessing REST API endpoints using {@link RestTemplate}.
 *
 * @author Alex Oreshkevich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
public class HelloControllerTest
{

    @Test
    public void test()
            throws Exception
    {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:48080/";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    }
}
