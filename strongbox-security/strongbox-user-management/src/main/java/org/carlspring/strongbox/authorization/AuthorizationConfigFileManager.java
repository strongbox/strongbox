package org.carlspring.strongbox.authorization;

import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.xml.XmlFileManager;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class AuthorizationConfigFileManager
        extends XmlFileManager<AuthorizationConfigDto>
{

    @Override
    public String getPropertyKey()
    {
        return "strongbox.authorization.config.xml";
    }

    @Override
    public String getDefaultLocation()
    {
        return "etc/conf/strongbox-authorization.xml";
    }

}
