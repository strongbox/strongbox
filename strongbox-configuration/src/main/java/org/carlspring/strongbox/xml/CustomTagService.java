package org.carlspring.strongbox.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomTagService
{

    private static final Logger logger = LoggerFactory.getLogger(CustomTagService.class);

    private static CustomTagService service;

    private ServiceLoader<CustomTag> loader;


    private CustomTagService()
    {
        loader = ServiceLoader.load(CustomTag.class);
    }

    public static synchronized CustomTagService getInstance()
    {
        if (service == null)
        {
            service = new CustomTagService();
        }

        return service;
    }

    public List<Class> getImplementations()
    {
        List<Class> implementations = new ArrayList<>();

        if (loader.iterator().hasNext())
        {
            logger.debug("Available custom tag implementations:");

            for (CustomTag tag : loader)
            {
                Class c = tag.getClass();

                logger.debug("- " + c.getCanonicalName());

                implementations.add(c);
            }
        }

        return implementations;
    }

}
