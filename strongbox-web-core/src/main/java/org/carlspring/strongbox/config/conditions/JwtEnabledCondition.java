package org.carlspring.strongbox.config.conditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class JwtEnabledCondition implements Condition
{

    private static final Logger logger = LoggerFactory.getLogger(JwtEnabledCondition.class);

    @Override
    public boolean matches(ConditionContext context,
                           AnnotatedTypeMetadata metadata)
    {
        final boolean jwtEnabled = !Boolean.parseBoolean(context.getEnvironment()
                                                                .getProperty("strongbox.security.jwt.disabled"));

        logger.trace("JWT enabled ? {}", jwtEnabled);

        return jwtEnabled;
    }

}
