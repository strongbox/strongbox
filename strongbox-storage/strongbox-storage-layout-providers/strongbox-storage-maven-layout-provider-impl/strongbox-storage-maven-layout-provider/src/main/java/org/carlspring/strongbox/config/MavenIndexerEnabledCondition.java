package org.carlspring.strongbox.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Przemyslaw Fusik
 */
public class MavenIndexerEnabledCondition
        implements Condition
{

    public static final String MAVEN_INDEXER_ENABLED = "maven.indexer.enabled";

    @Override
    public boolean matches(final ConditionContext context,
                           final AnnotatedTypeMetadata metadata)
    {
        return Boolean.parseBoolean(context.getEnvironment().getProperty(MAVEN_INDEXER_ENABLED));
    }
}
