package org.carlspring.strongbox.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.stereotype.Component;

/**
 * Custom expression handler.
 *
 * @author Alex Oreshkevich
 */
@Component
public class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler
{

    private static final Logger logger = LoggerFactory.getLogger(CustomMethodSecurityExpressionHandler.class);

    public CustomMethodSecurityExpressionHandler(){
        setDefaultRolePrefix("");
    }

    @Override
    public Object filter(Object filterTarget,
                         Expression filterExpression,
                         EvaluationContext ctx)
    {
        logger.debug("FILTERING>>>>> filterTarget " + filterTarget + " filterExpression " + filterExpression);
        return super.filter(filterTarget, filterExpression, ctx);
    }
}
