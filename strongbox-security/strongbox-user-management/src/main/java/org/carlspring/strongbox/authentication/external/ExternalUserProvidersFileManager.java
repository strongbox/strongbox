package org.carlspring.strongbox.authentication.external;

import org.carlspring.strongbox.xml.XmlFileManager;

/**
 * @author Przemyslaw Fusik
 */
public class ExternalUserProvidersFileManager
        extends XmlFileManager<ExternalUserProviders>
{

    public ExternalUserProvidersFileManager()
    {
        super(ExternalUserProvider.class);
    }

    @Override
    public String getPropertyKey()
    {
        return "strongbox.external.user.providers.xml";
    }

    @Override
    public String getDefaultLocation()
    {
        return "etc/conf/strongbox-external-user-providers.xml";
    }

}
