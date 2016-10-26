package org.carlspring.strongbox.rest.context;

import org.carlspring.strongbox.config.WebConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Helper meta annotation for all rest-assured based tests.
 *
 * @author Alex Oreshkevich
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
public @interface RestAssuredTest
{

}