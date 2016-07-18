package org.carlspring.strongbox.rest;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Alex Oreshkevich
 */
public class BackendBaseTest {

    @Autowired
    protected WebApplicationContext context;

    @Before
    public void init() {
        RestAssuredMockMvc.webAppContextSetup(context);
    }

    @After
    public void shutdown() {
        RestAssuredMockMvc.reset();
    }
}
