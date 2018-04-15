package org.carlspring.strongbox.config;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Przemyslaw Fusik
 */
public class MavenIndexerDisabledCondition
        extends MavenIndexerEnabledCondition
{

    @Override
    public boolean matches(final ConditionContext context,
                           final AnnotatedTypeMetadata metadata)
    {
        return !super.matches(context, metadata);
    }
}
