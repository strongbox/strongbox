package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.generator.RawArtifactGenerator;
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
 * @author Wojciech Pater
 */
@Documented
@Retention(RUNTIME)
@Target({ PARAMETER, ANNOTATION_TYPE})
@TestArtifact(generator = RawArtifactGenerator.class, strategy = RawArtifactGeneratorStrategy.class)
public @interface RawTestArtifact
{
    /**
     * {@link Storage} ID.
     */
    @AliasFor(annotation = TestArtifact.class)
    String storageId() default "storage-raw";

    /**
     * {@link Repository} ID.1
     */
    @AliasFor(annotation = TestArtifact.class)
    String repositoryId() default "";

    /**
     * Layout specific artifact URI.
     */
    @AliasFor(annotation = TestArtifact.class)
    String resource() default "";

    /**
     * The {@link org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates} name.
     */
    @AliasFor(annotation = TestArtifact.class)
    String id() default "";

    /**
     * The {@link org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates} version.
     */
    @AliasFor(annotation = TestArtifact.class)
    String[] versions() default {};

    /**
     * The {@link org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates} extension.
     */
    String extension() default "";

    /**
     * The {@link org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates} scope.
     */
    String scope() default "";

    /**
     * Additional artifact size in bytes.
     */
    @AliasFor(annotation = TestArtifact.class)
    long size() default 1000000;
}
