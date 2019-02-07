package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.xml.XmlFileManager;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ConfigurationFileManager
        extends XmlFileManager<MutableConfiguration>
{

    @Inject
    private ConfigurationResourceResolver configurationResourceResolver;


    @Override
    public String getPropertyKey()
    {
        return "strongbox.config.xml";
    }

    @Override
    public String getDefaultLocation()
    {
        return "etc/conf/strongbox.xml";
    }

    @Override
    public ConfigurationResourceResolver getConfigurationResourceResolver()
    {
        return configurationResourceResolver;
    }

}
