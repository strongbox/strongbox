package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.xml.XmlFileManager;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ConfigurationFileManager
        extends XmlFileManager<MutableConfiguration>
{

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

}
