package org.carlspring.strongbox.cron.config;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.domain.CronTasksConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ApplicationStartupCronTasksInitiator
{

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupCronTasksInitiator.class);
    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @PostConstruct
    public void postConstruct()
            throws Exception
    {
        CronTasksConfiguration cronTasksConfiguration = loadConfigurationFromFileSystem();
        for (final CronTaskConfiguration configuration : cronTasksConfiguration.getCronTaskConfigurations())
        {
            logger.debug("Saving cron configuration {}", configuration);
            cronTaskConfigurationService.saveConfiguration(configuration);
        }
    }

    private CronTasksConfiguration loadConfigurationFromFileSystem()
            throws IOException, JAXBException
    {

        final Resource resource = getConfigurationResource();

        logger.debug("Loading cron configuration from XML file {}", resource.getURI());

        GenericParser<CronTasksConfiguration> parser = new GenericParser<>(CronTasksConfiguration.class);
        CronTasksConfiguration configuration;

        try (final InputStream is = resource.getInputStream())
        {
            configuration = parser.parse(is);
        }

        return configuration;
    }

    public Resource getConfigurationResource()
            throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource("strongbox.cron.tasks.xml",
                                                                      "etc/conf/strongbox-cron-tasks.xml");
    }

}
