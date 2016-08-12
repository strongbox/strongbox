package org.carlspring.strongbox.rest.app;

import org.carlspring.strongbox.rest.exception.GenericExceptionMapper;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrongboxApplication
        extends ResourceConfig
{

    private static final Logger logger = LoggerFactory.getLogger(StrongboxApplication.class);


    public StrongboxApplication()
    {
        if (logger.isDebugEnabled())
        {
            register(new LoggingFilter());
        }

        // register exception mappers
		register(GenericExceptionMapper.class);
//		register(AppExceptionMapper.class);
//      register(CustomReasonPhraseExceptionMapper.class);
//		register(NotFoundExceptionMapper.class);
    }

  /*  @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("jersey.config.server.provider.packages", "org.carlspring.strongbox.rest");
        return properties;
    }*/

}
