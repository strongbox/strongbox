package org.carlspring.strongbox.users;

import org.carlspring.strongbox.users.dto.UsersDto;
import org.carlspring.strongbox.xml.XmlFileManager;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class UsersFileManager
        extends XmlFileManager<UsersDto>
{

    @Override
    public String getPropertyKey()
    {
        return "strongbox.users.config.xml";
    }

    @Override
    public String getDefaultLocation()
    {
        return "etc/conf/strongbox-security-users.xml";
    }

}
