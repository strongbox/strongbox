package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * @author mtodorov
 */
public abstract class AbstractConfigurationManager<T>
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractConfigurationManager.class);

    private String configurationPath;

    private ServerConfiguration<T> configuration;

    private GenericParser<T> parser;


    public AbstractConfigurationManager(Class... classes)
    {
        parser = new GenericParser<>(classes);
    }

    @PostConstruct
    public void init()
            throws IOException, JAXBException
    {
        Resource resource = getConfigurationResource();

        logger.info("Loading Strongbox configuration from " + resource.toString() + "...");

        //noinspection unchecked
        configuration = (ServerConfiguration<T>) parser.parse(resource.getInputStream());
        configuration.setResource(resource);
    }

    public void store()
            throws IOException, JAXBException
    {
        store(configuration);
    }

    public void store(ServerConfiguration<T> configuration)
            throws IOException, JAXBException
    {
        Resource resource = getConfigurationResource();

        //noinspection unchecked
        parser.store((T) configuration, resource.getFile());
    }

    public void store(ServerConfiguration<T> configuration, String file)
            throws IOException, JAXBException
    {
        //noinspection unchecked
        parser.store((T) configuration, file);
    }

    /**
     * Override this in your implementation with a cast.
     *
     * @return
     */
    public ServerConfiguration<T> getConfiguration()
    {
        return configuration;
    }


    public void setConfiguration(ServerConfiguration<T> configuration)
    {
        this.configuration = configuration;
    }

    public String getConfigurationPath()
    {
        return configurationPath;
    }

    public void setConfigurationPath(String configurationPath)
    {
        this.configurationPath = configurationPath;
    }

    public abstract Resource getConfigurationResource()
            throws IOException;

}
