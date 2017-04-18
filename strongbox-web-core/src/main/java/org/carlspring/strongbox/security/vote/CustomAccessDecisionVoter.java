package org.carlspring.strongbox.security.vote;

import org.carlspring.strongbox.controllers.maven.MavenArtifactController;
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
        // do not participate in voting directly by default
        int vote = ACCESS_ABSTAIN;

        this.authentication = authentication;

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

        List<GrantedAuthority> authorities = new ArrayList<>(authentication.getAuthorities());

        // assign default repository privileges
        Collection<String> defaultPrivileges = accessModel.getUrlToPrivilegesMap()
                                                          .get(storageId + "/" + repositoryId);
        if (defaultPrivileges != null)
        {
            defaultPrivileges.forEach(privilege -> authorities.add(new SimpleGrantedAuthority(privilege)));
        }

        // assign exact path matching privileges
        Collection<String> exactPathMatchPrivileges = accessModel.getUrlToPrivilegesMap()
                                                                 .get(UrlUtils.getRequestUri());
        if (exactPathMatchPrivileges != null)
        {
            exactPathMatchPrivileges.forEach(privilege -> authorities.add(new SimpleGrantedAuthority(privilege)));
        }

        // TODO assign path-specific privileges (using wildcards like .*)

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
