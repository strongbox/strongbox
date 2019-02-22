package org.carlspring.strongbox.users.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.UserAccessModelReadContract;

/**
 * @author sbespalov
 *
 */
public class AuthorizedAccessModel implements UserAccessModelReadContract
{
    
    private final UserAccessModelReadContract target;
    
    public AuthorizedAccessModel(UserAccessModelReadContract target)
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
