package org.carlspring.strongbox.security.vote;

import java.util.Collection;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.prepost.PreInvocationAuthorizationAdvice;
import org.springframework.security.access.prepost.PreInvocationAuthorizationAdviceVoter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Alex Oreshkevich
 */
public class CustomPreInvocationAuthorizationAdviceVoter
        extends PreInvocationAuthorizationAdviceVoter
{

    public CustomPreInvocationAuthorizationAdviceVoter(PreInvocationAuthorizationAdvice pre)
    {
        super(pre);
    }

    @Override
    public int vote(Authentication authentication,
                    MethodInvocation method,
                    Collection<ConfigAttribute> attributes)
    {
        // we need to use authentication from SecurityContextHolder here
        // because we changed it at runtime in custom AccessDecisionVoter
        // see CustomAccessDecisionVoter#vote() for details
        return super.vote(SecurityContextHolder.getContext()
                                               .getAuthentication(), method, attributes);
    }
}
