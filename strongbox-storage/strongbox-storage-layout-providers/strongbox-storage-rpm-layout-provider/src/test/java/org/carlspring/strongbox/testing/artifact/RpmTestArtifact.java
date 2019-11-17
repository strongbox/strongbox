package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.coordinates.RpmArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.RpmArtifactGenerator;
import org.carlspring.strongbox.domain.RpmPackageArch;
import org.carlspring.strongbox.domain.RpmPackageType;
import org.carlspring.strongbox.storage.Storage;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.maven.model.Repository;
import org.springframework.core.annotation.AliasFor;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author carlspring
 */
@Documented
@Retention(RUNTIME)
@Target({PARAMETER, ANNOTATION_TYPE})
@TestArtifact(generator = RpmArtifactGenerator.class, strategy = RpmArtifactGeneratorStrategy.class)
public @interface RpmTestArtifact
{

    /**
     * {@link Storage} ID.
     */
    @AliasFor(annotation = TestArtifact.class)
    String storageId() default "storage-rpm";

    /**
     * {@link Repository} ID.
     */
    @AliasFor(annotation = TestArtifact.class)
    String repositoryId() default "releases-rpm";

    @AliasFor(annotation = TestArtifact.class)
    String id() default "";

    @AliasFor(annotation = TestArtifact.class)
    String resource() default "";    
    
    /**
     * The {@link RpmArtifactCoordinates} versions.
     */
    @AliasFor(annotation = TestArtifact.class)
    String[] versions() default {};
    
    String baseName() default "";

    String release() default "";

    String architecture() default "noarch";

    String packageType() default "";

    String getExtension() default "";

    /**
     * Additional artifact size in bytes.
     */
    @AliasFor(annotation = TestArtifact.class)
    long bytesSize() default 1000000;

}
