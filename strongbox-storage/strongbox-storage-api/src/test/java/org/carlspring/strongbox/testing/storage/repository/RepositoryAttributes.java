package org.carlspring.strongbox.testing.storage.repository;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;

/**
 * This annotation provice the common {@link Repository} attributes
 * configuration support.
 * 
 * @author sbespalov
 * 
 * @see Repository
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface RepositoryAttributes
{

    RepositoryStatusEnum status() default RepositoryStatusEnum.IN_SERVICE;

    boolean trashEnabled() default false;

    boolean allowsDelete() default true;

    boolean allowsDeployment() default true;

    boolean allowsDirectoryBrowsing() default true;

    boolean allowsForceDeletion() default false;

    boolean allowsRedeployment() default true;

    boolean checksumHeadersEnabled() default false;

}
