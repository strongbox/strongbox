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

    private List<Class> implementations = new ArrayList<>();


    private CustomTagService()
    {
        loader = ServiceLoader.load(CustomTag.class);

        loadImplementations();
    }

    public static synchronized CustomTagService getInstance()
    {
        if (service == null)
        {
            service = new CustomTagService();
        }

        return service;
    }

    private void loadImplementations()
    {
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
    }

    public List<Class> getImplementations()
    {
        return implementations;
    }

}
