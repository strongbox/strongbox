package org.carlspring.strongbox.rest.common;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.web.context.WebApplicationContext;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

/**
 * @author sbespalov
 *
 */
public class RestAssuredTestExecutionListener extends AbstractTestExecutionListener
{

    @Override
    public void beforeTestClass(TestContext testContext)
        throws Exception
    {
        WebApplicationContext applicationContext = (WebApplicationContext) testContext.getApplicationContext();
        
        RestAssuredMockMvc.webAppContextSetup(applicationContext);

    }

    @Override
    public void afterTestClass(TestContext testContext)
        throws Exception
    {
        RestAssuredMockMvc.reset();
    }

}
