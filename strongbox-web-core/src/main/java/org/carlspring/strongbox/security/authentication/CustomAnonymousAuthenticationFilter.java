package org.carlspring.strongbox.security.authentication;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * Custom anonymous authentication filter that allows us to change the anonymous authorities at runtime.
 *
 * @author Alex Oreshkevich
 * @see {@linkplain https://dev.carlspring.org/youtrack/issue/SB-606}
 */
public class CustomAnonymousAuthenticationFilter
        extends AnonymousAuthenticationFilter
{

    public CustomAnonymousAuthenticationFilter(String key,
                                               Object principal,
                                               List<GrantedAuthority> authorities)
    {
        super(key, principal, authorities);
    }
}
