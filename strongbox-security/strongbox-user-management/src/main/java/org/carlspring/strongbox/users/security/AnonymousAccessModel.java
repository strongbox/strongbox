package org.carlspring.strongbox.users.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.AccessModel;


/**
 * @author sbespalov
 *
 */
public class AnonymousAccessModel implements AccessModel
{
    
    private final AccessModel target;
    
    public AnonymousAccessModel(AccessModel target)
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
    public Set<Privileges> getPathAuthorities(String url)
    {
        return target.getPathAuthorities(url);
    }

}