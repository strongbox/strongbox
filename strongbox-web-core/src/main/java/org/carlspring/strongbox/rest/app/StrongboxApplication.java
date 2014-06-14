package org.carlspring.strongbox.rest.app;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrongboxApplication extends ResourceConfig
{

    private static final Logger logger = LoggerFactory.getLogger(StrongboxApplication.class);


    public StrongboxApplication()
    {
        if (logger.isDebugEnabled())
        {
            register(new LoggingFilter());
        }
    }

}
