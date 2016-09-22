package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.config.WebConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by yury on 9/20/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
public class HelloControllerTest extends BackendBaseTest {

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testSetAndGetPort()
            throws Exception {
        String url = "http://localhost:48080" + "/yury/storages/greet";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    }
}
