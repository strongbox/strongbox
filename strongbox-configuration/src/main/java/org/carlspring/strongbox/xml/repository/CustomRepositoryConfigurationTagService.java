package org.carlspring.strongbox.xml.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author carlspring
 */
public class CustomRepositoryConfigurationTagService
{

    private static final Logger logger = LoggerFactory.getLogger(CustomRepositoryConfigurationTagService.class);

    private static CustomRepositoryConfigurationTagService service;

    private ServiceLoader<RepositoryConfiguration> loader;


    private CustomRepositoryConfigurationTagService()
    {
        loader = ServiceLoader.load(RepositoryConfiguration.class);
    }

    public static synchronized CustomRepositoryConfigurationTagService getInstance()
    {
        if (service == null)
        {
            service = new CustomRepositoryConfigurationTagService();
        }

        return service;
    }

    public List<Class> getImplementations()
    {
        List<Class> implementations = new ArrayList<>();

        if (loader.iterator().hasNext())
        {
            logger.debug("Available custom tag implementations:");

            for (RepositoryConfiguration tag : loader)
            {
                Class c = tag.getClass();

                logger.debug("- " + c.getCanonicalName());

                implementations.add(c);
            }
        }

        return implementations;
    }

}
