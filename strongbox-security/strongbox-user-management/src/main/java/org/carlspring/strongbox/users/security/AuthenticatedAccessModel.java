package org.carlspring.strongbox.users.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.AccessModelData;

/**
 * @author sbespalov
 *
 */
public class AuthenticatedAccessModel implements AccessModelData
{
    
    private final AccessModelData target;
    
    public AuthenticatedAccessModel(AccessModelData target)
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
    public Set<Privileges> getPathAuthorities(String url)
    {
        return target.getPathAuthorities(url);
    }

}
