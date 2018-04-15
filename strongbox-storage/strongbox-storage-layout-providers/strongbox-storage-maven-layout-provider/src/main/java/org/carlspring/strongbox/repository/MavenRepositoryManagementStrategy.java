package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.config.MavenIndexerDisabledCondition;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
@Conditional(MavenIndexerDisabledCondition.class)
public class MavenRepositoryManagementStrategy
        extends AbstractRepositoryManagementStrategy
{

}
