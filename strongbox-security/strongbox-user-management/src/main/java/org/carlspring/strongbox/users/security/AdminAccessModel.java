package org.carlspring.strongbox.users.security;

import java.util.Set;

import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.AccessModelData;

/**
 * @author sbespalov
 *
 */
public class AdminAccessModel implements AccessModelData
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
