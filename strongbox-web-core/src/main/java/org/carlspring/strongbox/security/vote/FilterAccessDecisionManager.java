package org.carlspring.strongbox.security.vote;

import java.util.Arrays;

import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.stereotype.Component;

/**
 * @author Alex Oreshkevich
 */
@Component
public class FilterAccessDecisionManager
        extends AffirmativeBased
        implements AccessDecisionManager
{

    @SuppressWarnings("unchecked")
    public FilterAccessDecisionManager()
    {
        super(Arrays.asList(new CustomAccessDecisionVoter(),
                            new WebExpressionVoter(),
                            new RoleVoter(),
                            new AuthenticatedVoter()
        ));
    }
}
