package org.carlspring.strongbox.users.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.AccessModelData;

/**
 * @author sbespalov
 *
 */
public class AuthorizedAccessModel implements AccessModelData
{
    
    private final AccessModelData target;
    
    public AuthorizedAccessModel(AccessModelData target)
    {
        this.target = target;
    }

    public Set<Privileges> getApiAuthorities()
    {
        Set<Privileges> authorities = new HashSet<Privileges>(target.getApiAuthorities());
        authorities.add(Privileges.AUTHENTICATED_USER);
        
        return Collections.unmodifiableSet(authorities);
    }

    @Override
    public Collection<Privileges> getPathPrivileges(String url)
    {
        return target.getPathPrivileges(url);
    }

}
