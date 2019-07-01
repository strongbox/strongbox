package org.carlspring.strongbox.users.security;

import java.util.Collection;
import java.util.Set;

import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.AccessModelData;
import org.carlspring.strongbox.users.dto.StoragePrivilegesData;

/**
 * @author sbespalov
 *
 */
public class AdminAccessModel implements AccessModelData
{
    
    private final AccessModelData target;
    
    public AdminAccessModel(AccessModelData target)
    {
        this.target = target;
    }

    public Set<Privileges> getApiAuthorities()
    {
        return Privileges.all();
    }

    @Override
    public Collection<Privileges> getPathPrivileges(String url)
    {
        return Privileges.all();
    }

}
