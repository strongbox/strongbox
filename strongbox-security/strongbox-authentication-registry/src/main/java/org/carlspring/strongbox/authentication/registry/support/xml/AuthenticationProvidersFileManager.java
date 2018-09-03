package org.carlspring.strongbox.authentication.registry.support.xml;

import org.carlspring.strongbox.authentication.XmlAuthenticationProviders;
import org.carlspring.strongbox.xml.XmlFileManager;

/**
 * @author Przemyslaw Fusik
 */
public class AuthenticationProvidersFileManager
        extends XmlFileManager<XmlAuthenticationProviders>
{

    @Override
    public String getPropertyKey()
    {
        return "strongbox.authentication.providers.xml";
    }

    @Override
    public String getDefaultLocation()
    {
        return "etc/conf/strongbox-authentication-providers.xml";
    }

}
