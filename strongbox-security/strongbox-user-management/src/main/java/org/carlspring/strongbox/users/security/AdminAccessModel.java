package org.carlspring.strongbox.users.security;

import java.util.Set;

import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.AccessModel;

/**
 * @author sbespalov
 *
 */
public class AdminAccessModel implements AccessModel
{
    
    public Set<Privileges> getApiAuthorities()
    {
        return Privileges.all();
    }

    @Override
    public Set<Privileges> getPathAuthorities(String url)
    {
        return Privileges.all();
    }

}
