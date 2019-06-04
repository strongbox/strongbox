package org.carlspring.strongbox.security.vote;

import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.dto.UserAccessModelReadContract;
import org.carlspring.strongbox.users.userdetails.SpringSecurityUser;
import org.carlspring.strongbox.utils.UrlUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.expression.method.ExpressionBasedPreInvocationAdvice;
import org.springframework.security.access.prepost.PreInvocationAuthorizationAdviceVoter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.web.Constants.ARTIFACT_ROOT_PATH;

/**
 * @author sbespalov
 *
 */
@Component
public class ExtendedAuthoritiesVoter extends PreInvocationAuthorizationAdviceVoter
{

    public ExtendedAuthoritiesVoter()
    {
        super(new ExpressionBasedPreInvocationAdvice());
    }

    @Override
    public int vote(Authentication authentication,
                    MethodInvocation method,
                    Collection<ConfigAttribute> attributes)
    {
        return super.vote(new ExtendedAuthorityAuthentication(authentication), method, attributes);
    }

    @SuppressWarnings("serial")
    private class ExtendedAuthorityAuthentication implements Authentication
    {

        private Authentication source;

        public ExtendedAuthorityAuthentication(Authentication target)
        {
            super();
            this.source = target;
        }

        private Authentication getSourceAuthentication()
        {
            return source;
        }

        private Collection<? extends GrantedAuthority> calculateExtendedAuthorities(Authentication authentication)
        {
            Object principal = authentication.getPrincipal();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            logger.debug(String.format("Privileges for [%s] are [%s]", principal,
                                       authorities));

            if (!authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken)
            {

                return authentication.getAuthorities();
            }
            else if (!(principal instanceof SpringSecurityUser))
            {

                logger.warn(String.format("Unknown authentication principal type [%s]", principal.getClass()));

                return authentication.getAuthorities();
            }

            SpringSecurityUser userDetails = (SpringSecurityUser) authentication.getPrincipal();
            UserAccessModelReadContract accessModel = userDetails.getAccessModel();
            if (accessModel == null)
            {
                return authorities;
            }

            String requestUri = UrlUtils.getRequestUri();
            if (!requestUri.startsWith(ARTIFACT_ROOT_PATH))
            {
                return authorities;
            }

            String storageId = UrlUtils.getCurrentStorageId();
            String repositoryId = UrlUtils.getCurrentRepositoryId();
            if (storageId == null || repositoryId == null)
            {
                return authorities;
            }

            // assign privileges based on custom user access model
            final Collection<String> customAuthorities = AccessModel.getPathPrivileges(accessModel, UrlUtils.getRequestUri());
            if (customAuthorities == null || customAuthorities.isEmpty())
            {
                return authorities;
            }

            List<GrantedAuthority> extendedAuthorities = new ArrayList<>(authorities);
            customAuthorities.forEach(privilege -> extendedAuthorities.add(new SimpleGrantedAuthority(privilege)));
            logger.debug(String.format("Privileges for [%s] was extended to [%s]", userDetails.getUsername(),
                                       extendedAuthorities));

            return extendedAuthorities;
        }

        public String getName()
        {
            return getSourceAuthentication().getName();
        }

        public Collection<? extends GrantedAuthority> getAuthorities()
        {
            return calculateExtendedAuthorities(getSourceAuthentication());
        }

        public Object getCredentials()
        {
            return getSourceAuthentication().getCredentials();
        }

        public Object getDetails()
        {
            return getSourceAuthentication().getDetails();
        }

        public Object getPrincipal()
        {
            return getSourceAuthentication().getPrincipal();
        }

        public boolean isAuthenticated()
        {
            return getSourceAuthentication().isAuthenticated();
        }

        public void setAuthenticated(boolean isAuthenticated)
            throws IllegalArgumentException
        {
            getSourceAuthentication().setAuthenticated(isAuthenticated);
        }

    }
}
