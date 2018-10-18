package org.carlspring.strongbox.xml.repository.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author carlspring
 */
public class RemoteRepositoryConfigurationTagService
{

    private static final Logger logger = LoggerFactory.getLogger(RemoteRepositoryConfigurationTagService.class);

    private static RemoteRepositoryConfigurationTagService service;

    private ServiceLoader<RemoteRepositoryConfiguration> loader;


    private RemoteRepositoryConfigurationTagService()
    {
        loader = ServiceLoader.load(RemoteRepositoryConfiguration.class);
    }

    public static synchronized RemoteRepositoryConfigurationTagService getInstance()
    {
        if (service == null)
        {
            service = new RemoteRepositoryConfigurationTagService();
        }

        return service;
    }

    public List<Class> getImplementations()
    {
        List<Class> implementations = new ArrayList<>();

        if (loader.iterator().hasNext())
        {
            logger.debug("Available custom tag implementations:");

            for (RemoteRepositoryConfiguration tag : loader)
            {
                Class c = tag.getClass();

                logger.debug("- " + c.getCanonicalName());

                implementations.add(c);
            }
        }

        return implementations;
    }

}
