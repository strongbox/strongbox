package org.carlspring.strongbox.security.vote;

import org.carlspring.strongbox.security.user.SpringSecurityUser;
import org.carlspring.strongbox.users.domain.Features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.FilterInvocation;

/**
 * Customization of {@link AccessDecisionVoter} according to custom access strategies for users
 * like per-repository access etc.
 *
 * @author Alex Oreshkevich
 * @see https://youtrack.carlspring.org/issue/SB-603
 */
public class CustomAccessDecisionVoter
        implements AccessDecisionVoter, AuthenticationProvider
{

    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDecisionVoter.class);

    private Authentication authentication;

    @Override
    public boolean supports(ConfigAttribute attribute)
    {
        // don't need any specific configuration attributes
        return true;
    }

    @Override
    public int vote(Authentication authentication,
                    Object object,
                    Collection collection)
    {
        int vote = ACCESS_ABSTAIN;
        this.authentication = authentication;

        SpringSecurityUser user = (SpringSecurityUser) authentication.getPrincipal();
        Features features = user.getFeatures();
        if (features == null)
        {
            return vote;
        }

        // save request url during first authentication phase
        // parse repositoryId from pre-saved during second authentication phase
        if (object instanceof FilterInvocation)
        {
            user.setUrl(((FilterInvocation) object).getRequestUrl());
        }
        else if (object instanceof ProxyMethodInvocation)
        {

            String repositoryId = getRepositoryIdFromUrl(user.getUrl());
            logger.debug("Load additional repository privileges by ID " + repositoryId);

            Collection<String> privilegeNames = user.getFeatures()
                                                    .getPerRepositoryAuthorities()
                                                    .get(repositoryId);
            if (privilegeNames != null)
            {
                List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authentication.getAuthorities());
                privilegeNames.forEach(name ->
                                       {
                                           SimpleGrantedAuthority authority = new SimpleGrantedAuthority(name);
                                           updatedAuthorities.add(authority);
                                           logger.debug("\tAdd " + name + " privilege...");
                                       });
                user.setAuthorities(updatedAuthorities);
                this.authentication = new UsernamePasswordAuthenticationToken(user,
                                                                              authentication.getCredentials(),
                                                                              updatedAuthorities);
            }
        }

        // do not participate in voting directly
        return vote;
    }

    @SuppressWarnings("unused")
    private String getStorageIdFromUrl(String url)
    {
        if (!url.startsWith("/"))
        {
            url = "/" + url;
        }
        return url.split("/")[2];
    }

    private String getRepositoryIdFromUrl(String url)
    {
        if (!url.startsWith("/"))
        {
            url = "/" + url;
        }
        return url.split("/")[3];
    }

    @Override
    public boolean supports(Class clazz)
    {
        // our voter is not concerned with the secured object type
        return true;
    }

    public Authentication getAuthentication()
    {
        return authentication;
    }
}
