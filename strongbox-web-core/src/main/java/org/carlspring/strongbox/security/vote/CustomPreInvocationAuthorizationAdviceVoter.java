package org.carlspring.strongbox.security.vote;

import java.util.Collection;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.prepost.PreInvocationAuthorizationAdvice;
import org.springframework.security.access.prepost.PreInvocationAuthorizationAdviceVoter;
import org.springframework.security.core.Authentication;

public class CustomPreInvocationAuthorizationAdviceVoter
        extends PreInvocationAuthorizationAdviceVoter
{

    private static final Logger logger = LoggerFactory.getLogger(CustomPreInvocationAuthorizationAdviceVoter.class);

    private AuthenticationProvider authenticationProvider;

    public CustomPreInvocationAuthorizationAdviceVoter(PreInvocationAuthorizationAdvice pre,
                                                       AuthenticationProvider authenticationProvider)
    {
        super(pre);
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public int vote(Authentication authentication,
                    MethodInvocation method,
                    Collection<ConfigAttribute> attributes)
    {
        logger.debug("Using authentication " + authenticationProvider.getAuthentication());
        return super.vote(authenticationProvider.getAuthentication(), method, attributes);
    }
}
