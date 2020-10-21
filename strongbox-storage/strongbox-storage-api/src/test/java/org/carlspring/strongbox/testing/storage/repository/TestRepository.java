package org.carlspring.strongbox.testing.storage.repository;

import org.carlspring.strongbox.providers.storage.FileSystemStorageProvider;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum;

import java.lang.annotation.*;
import java.net.URL;

import org.springframework.core.annotation.AliasFor;

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
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
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
    String storageId() default "storage0";

    /**
     * {@link Repository} ID.
     */
    String repositoryId() default "releases";

    /**
     * The storage provider.
     */
    String storageProvider() default FileSystemStorageProvider.ALIAS;

    /**
     * {@link RepositoryPolicyEnum}
     */
    RepositoryPolicyEnum policy() default RepositoryPolicyEnum.RELEASE;

    /**
     * {@link RepositorySetup} strategies to use within {@link Repository}
     * initialization.
     */
    Class<? extends RepositorySetup>[] setup() default {};

    /**
     * In case of <code>true</code> (default) will delete repository instance
     * and cleanup the repository folder after test method complete
     * execution.<br>
     * Please note that this flag assumed always to be <code>true</code>, the
     * only reason to set it as <code>false</code> is when you want to get the
     * test execution result for debug purpose.
     */
    boolean cleanup() default true;

    /**
     * {@link RemoteRepository}
     * configuration support.
     * 
     * @author sbespalov
     *
     */
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface Remote
    {

        /**
         * remote {@link URL}
         */
        String url();

    }

    /**
     * Group Repository configuration support.
     * 
     * @author sbespalov
     *
     */
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface Group
    {

        /**
         * Alias for `repositories`.
         */
        @AliasFor(attribute = "repositories")
        String[] value() default {};
        
        /**
         * Group member repositories list.
         */
        @AliasFor(attribute = "value")
        String[] repositories() default {};

        /**
         * Routing rules.
         */
        Rule[] rules() default {};
        
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @interface Rule
        {

            String[] repositories();
            
            String pattern() default "*";
            
            RoutingRuleTypeEnum type() default RoutingRuleTypeEnum.ACCEPT;

        }

    }

}
