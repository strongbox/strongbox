package org.carlspring.strongbox.rest.context;

import org.carlspring.strongbox.config.WebConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Meta annotation for generalised test execution context management.
 *
 * @author Alex Oreshkevich
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(classes = { WebConfig.class })
@Transactional
@Rollback
public @interface RestletTestContext
{

}