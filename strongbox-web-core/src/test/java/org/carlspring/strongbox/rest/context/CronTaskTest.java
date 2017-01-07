package org.carlspring.strongbox.rest.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.annotation.IfProfileValue;

/**
 * Marks test to be executed only under special spring active profile.
 *
 * @author Alex Oreshkevich
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@IfProfileValue(name = "spring.profiles.active",
                values = { "quartz-integration-test" })
public @interface CronTaskTest
{

}
