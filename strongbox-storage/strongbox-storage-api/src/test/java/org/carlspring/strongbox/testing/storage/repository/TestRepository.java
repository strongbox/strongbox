package org.carlspring.strongbox.testing.storage.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * This annotation provide ability to inject {@link Repository} instance as test
 * method parameters.<br>
 * Each repository will be bound to the test life cycle, so it will be created
 * before the test method execution and removed after test method execution.<br>
 * There is also provided the repository synchronization between threads, so
 * every repository will be locked within test method execution thread and other
 * threads will wait until it released.
 * 
 * @author sbespalov
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TestRepository
{

    /**
     * {@link Repository} layout.
     */
    String layout();

    /**
     * {@link Storage} ID.
     */
    String storage();

    /**
     * {@link Repository} ID.
     */
    String repository();

    /**
     * In case of <code>true</code> (default) will delete repository instance
     * and cleanup the repository folder after test method complete
     * execution.<br>
     * Please note that this flag assumed always to be <code>true</code>, the
     * only reason to set it as <code>false</code> is when you want to get the
     * test execution result for debug purpose.
     */
    boolean cleanup() default true;


}
