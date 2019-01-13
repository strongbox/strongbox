package org.carlspring.strongbox.validation.cron;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author Pablo Tirado
 */
@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.METHOD,
          ElementType.FIELD,
          ElementType.ANNOTATION_TYPE,
          ElementType.CONSTRUCTOR,
          ElementType.PARAMETER,
          ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueCronTaskConfiguration
{

    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}