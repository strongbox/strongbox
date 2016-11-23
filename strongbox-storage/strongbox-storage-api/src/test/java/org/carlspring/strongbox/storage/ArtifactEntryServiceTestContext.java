package org.carlspring.strongbox.storage;

import org.carlspring.strongbox.config.DataServiceConfig;

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
 * @author Martin Todorov
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(classes = { DataServiceConfig.class })
@Transactional
@Rollback // change to false if you wanna inspect database content manually
public @interface ArtifactEntryServiceTestContext
{

}