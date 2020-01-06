package org.carlspring.strongbox.rest.common;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.WebApplicationContext;

import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;

/**
 * 
 * @author ankit.tomar
 *
 */
public abstract class PypiRestAssuredBaseTest
{

    protected static final String REPOSITORY_RELEASES = "pypi-releases-test";

    protected static final String REPOSITORY_STORAGE = "storage-pypi-test";

    protected static final String PIP_USER_AGENT = "pip/*";

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Inject
    protected WebApplicationContext context;

    @Value("${strongbox.url}")
    private String contextBaseUrl;

    @Inject
    protected MockMvcRequestSpecification mockMvc;


    public String getContextBaseUrl()
    {
        return contextBaseUrl;
    }

    public void setContextBaseUrl(String contextBaseUrl)
    {
        this.contextBaseUrl = contextBaseUrl;
    }

}
