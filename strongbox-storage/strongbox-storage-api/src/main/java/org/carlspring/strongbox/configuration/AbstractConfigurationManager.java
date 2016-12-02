package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

/**
 * @author mtodorov
 */
public abstract class AbstractConfigurationManager<T>
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractConfigurationManager.class);
    
    protected ServerConfiguration configuration;
    
    @Autowired
    protected ConfigurationRepository configurationRepository;
    
    private String configurationPath;
    
    private GenericParser<T> parser;


    public AbstractConfigurationManager(Class... classes)
    {
        parser = new GenericParser<>(classes);
    }

    @PostConstruct
    public synchronized void init()
            throws IOException, JAXBException
    {
        this.configuration = configurationRepository.getConfiguration();

        logger.info("Loading Strongbox configuration from OrientDB...");

        if (configuration == null)
        {
            throw new BeanCreationException("Unable to load configuration from db");
        }
    }

    public synchronized void store()
            throws IOException, JAXBException
    {
        store(configuration);
    }

    public synchronized void store(ServerConfiguration configuration)
            throws IOException, JAXBException
    {
        try
        {
            Configuration configurationCasted = (Configuration) configuration;
            configurationRepository.updateConfiguration(configurationCasted);
        }
        catch (ClassCastException e)
        {
            logger.error(configuration.getClass().getName() + " is not supported", e);
        }
        catch (Exception e)
        {
            logger.error("Unable to store", e);
        }
    }

    public synchronized void store(ServerConfiguration configuration,
                                   String file)
            throws IOException, JAXBException
    {
        //noinspection unchecked
        parser.store((T) configuration, file);
    }

    /**
     * Override this in your implementation with a cast.
     */
    public ServerConfiguration getConfiguration()
    {
        return configuration;
    }


    public void setConfiguration(ServerConfiguration configuration)
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
