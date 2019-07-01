package org.carlspring.strongbox.users.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.AccessModelData;
import org.carlspring.strongbox.users.dto.StoragePrivilegesData;


/**
 * @author sbespalov
 *
 */
public class AnonymousAccessModel implements AccessModelData
{
    
    private final AccessModelData target;
    
    public AnonymousAccessModel(AccessModelData target)
    {
        this.target = target;
    }

    public Set<Privileges> getApiAuthorities()
    {
        Set<Privileges> authorities = new HashSet<Privileges>(target.getApiAuthorities());
        authorities.add(Privileges.ANONYMOUS_USER);
        
        return Collections.unmodifiableSet(authorities);
    }

    @Override
    public Collection<Privileges> getPathPrivileges(String url)
    {
        return target.getPathPrivileges(url);
    }

}