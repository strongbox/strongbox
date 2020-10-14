package org.carlspring.strongbox.web;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.carlspring.strongbox.db.schema.Properties.REPOSITORY_ID;
import static org.carlspring.strongbox.db.schema.Properties.STORAGE_ID;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation refers to a controller method parameter related to a Repository which is going to be validated
 * for provided path variables.
 *
 * @author Pablo Tiraoo
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface RepositoryMapping
{

    String storageVariableName() default STORAGE_ID;

    String repositoryVariableName() default REPOSITORY_ID;

    boolean allowOutOfServiceRepository() default false;
}
