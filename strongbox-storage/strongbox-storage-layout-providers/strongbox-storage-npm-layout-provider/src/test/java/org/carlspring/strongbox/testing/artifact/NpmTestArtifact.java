package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.generator.NpmArtifactGenerator;
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
 * @author Yuri Zaytsev
 *
 */
@Documented
@Retention(RUNTIME)
@Target({ PARAMETER, ANNOTATION_TYPE})
@TestArtifact(generator = NpmArtifactGenerator.class, strategy = NpmArtifactGeneratorStrategy.class)
public @interface NpmTestArtifact
{
    /**
     * {@link Storage} ID.
     */
    @AliasFor(annotation = TestArtifact.class)
    String storageId() default "storage-npm";

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
     * The {@link org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates} name.
     */
    @AliasFor(annotation = TestArtifact.class)
    String id() default "";

    /**
     * The {@link org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates} version.
     */
    @AliasFor(annotation = TestArtifact.class)
    String[] versions() default {};

    /**
     * Additional artifact size in bytes.
     */
    @AliasFor(annotation = TestArtifact.class)
    long bytesSize() default 1000000;

    /**
     * The {@link org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates} extension.
     */
    String extension() default "tgz";

    /**
     * The {@link org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates} scope.
     */
    String scope() default "";
    
    /**
     * License Configuration for test artifact
     * @see {https://docs.npmjs.com/cli/v6/configuring-npm/package-json#license}
     */
    LicenseConfiguration[] licenses() default {};
    
}
