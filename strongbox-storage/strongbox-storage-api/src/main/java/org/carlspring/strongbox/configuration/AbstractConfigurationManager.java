package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.xml.parsers.GenericParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * @author mtodorov
 */
public abstract class AbstractConfigurationManager<T>
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractConfigurationManager.class);

    private String configurationPath;

    protected ServerConfiguration<T> configuration;

    private GenericParser<T> parser;

    @Autowired
    protected ConfigurationRepository configurationRepository;

    public AbstractConfigurationManager(Class... classes)
    {
        parser = new GenericParser<T>(classes);
    }

    @PostConstruct
    public void init()
            throws IOException, JAXBException
    {
        this.configuration = configurationRepository.getConfiguration();
        logger.info("Loading Strongbox configuration from orientdb ...");

    }

    public void store()
            throws IOException, JAXBException
    {
        store(configuration);
    }

    public void store(ServerConfiguration<T> configuration)
            throws IOException, JAXBException
    {
        configurationRepository.updateConfiguration(configuration);
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
