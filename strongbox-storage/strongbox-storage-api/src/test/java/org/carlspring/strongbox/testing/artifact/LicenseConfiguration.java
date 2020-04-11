package org.carlspring.strongbox.testing.artifact;

import java.lang.annotation.*;

/**
 * This annotation helps configure license details for test artifacts.
 *
 * @author carlspring
 */
@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LicenseConfiguration
{

    LicenseType license() default LicenseType.NONE;

    String destinationPath() default "LICENSE";

}
