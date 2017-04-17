package org.carlspring.strongbox.security.vote;

import org.carlspring.strongbox.controller.ArtifactController;
import org.carlspring.strongbox.security.user.SpringSecurityUser;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.utils.UrlUtils;

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
        AccessModel accessModel = user.getAccessModel();
        if (accessModel == null)
        {
            return vote;
        }

        String requestUri = UrlUtils.getRequestUri();
        if (!requestUri.startsWith(ArtifactController.ROOT_CONTEXT))
        {
            return vote;
        }

        String repositoryId;
        if (object instanceof ProxyMethodInvocation && (repositoryId = UrlUtils.getCurrentRepositoryId()) != null)
        {
            logger.debug("Load additional privileges by repository ID " + repositoryId);
            Collection<String> privilegeNames = user.getAccessModel()
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
