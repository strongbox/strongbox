package org.carlspring.strongbox.users.security;

import java.util.Collection;
import java.util.Set;

import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.UserAccessModelReadContract;
import org.carlspring.strongbox.users.dto.UserStorageReadContract;

/**
 * @author sbespalov
 *
 */
public class AdminAccessModel implements UserAccessModelReadContract
{
    
    private final UserAccessModelReadContract target;
    
    public AdminAccessModel(UserAccessModelReadContract target)
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
