package org.carlspring.strongbox.testing.artifact;

import java.lang.annotation.*;

import org.springframework.core.annotation.AliasFor;

/**
 * This annotation helps configure license details for test artifacts.
 *
 * @author carlspring
 */
@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@LicenseConfiguration
public @interface MavenLicenseConfiguration
{

    @AliasFor(annotation = LicenseConfiguration.class)
    LicenseType license() default LicenseType.NONE;

    @AliasFor(annotation = LicenseConfiguration.class)
    String destinationPath() default "LICENSE";

    boolean generateManifestEntry() default true;

}
