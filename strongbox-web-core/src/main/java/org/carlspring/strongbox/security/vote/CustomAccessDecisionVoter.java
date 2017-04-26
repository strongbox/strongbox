package org.carlspring.strongbox.security.vote;

import org.carlspring.strongbox.authentication.api.impl.userdetails.SpringSecurityUser;
import org.carlspring.strongbox.controllers.maven.MavenArtifactController;
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
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Customization of {@link AccessDecisionVoter} according to custom access strategies for users
 * like per-repository access etc.
 *
 * @author Alex Oreshkevich
 * @see https://youtrack.carlspring.org/issue/SB-603
 */
public class CustomAccessDecisionVoter
        implements AccessDecisionVoter
{

    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDecisionVoter.class);


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
        // do not participate in voting directly by default
        int vote = ACCESS_ABSTAIN;

        SpringSecurityUser user = (SpringSecurityUser) authentication.getPrincipal();
        AccessModel accessModel = user.getAccessModel();
        if (accessModel == null)
        {
            return vote;
        }

        String requestUri = UrlUtils.getRequestUri();
        if (!requestUri.startsWith(MavenArtifactController.ROOT_CONTEXT))
        {
            return vote;
        }

        if (!(object instanceof ProxyMethodInvocation))
        {
            return vote;
        }

        String storageId = UrlUtils.getCurrentStorageId();
        String repositoryId = UrlUtils.getCurrentRepositoryId();
        if (storageId == null || repositoryId == null)
        {
            return vote;
        }

        // assign privileges based on custom user access model
        final Collection<String> customAuthorities = accessModel.getPathPrivileges(UrlUtils.getRequestUri());
        if (customAuthorities != null && !customAuthorities.isEmpty())
        {
            List<GrantedAuthority> authorities = new ArrayList<>(authentication.getAuthorities());
            customAuthorities.forEach(privilege -> authorities.add(new SimpleGrantedAuthority(privilege)));

            logger.debug("Privileges was extended to " + authorities);

            SecurityContextHolder.getContext()
                                 .setAuthentication(new UsernamePasswordAuthenticationToken(user,
                                                                                            authentication.getCredentials(),
                                                                                            authorities));
        }

        return vote;
    }

    @Override
    public boolean supports(Class clazz)
    {
        // our voter is not concerned with the secured object type
        return true;
    }
}