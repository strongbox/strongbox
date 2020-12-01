package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.PypiArtifactGenerator;
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
@TestArtifact(generator = PypiArtifactGenerator.class, strategy = PypiArtifactGeneratorStrategy.class)
public @interface PypiTestArtifact
{
    /**
     * {@link Storage} ID.
     */
    @AliasFor(annotation = TestArtifact.class)
    String storageId() default "storage0";

    /**
     * {@link Repository} ID.1
     */
    @AliasFor(annotation = TestArtifact.class)
    String repositoryId() default "";

    /**
     * Layout specific artifact URI (ex.'path/to/strbox-1.0-none-any.whl').
     */
    @AliasFor(annotation = TestArtifact.class)
    String resource() default "";

    /**
     * The artifact distribution (ex. mysql-connector).
     */
    @AliasFor(annotation = TestArtifact.class)
    String id() default "";

    /**
     * The {@link PypiArtifactCoordinates} versions.
     */
    @AliasFor(annotation = TestArtifact.class)
    String[] versions() default {};

    /**
     * Pypi artifact packaging.
     * Supported packages: tar.gz, whl
     */
    String packaging() default "whl";

    /**
     * Additional artifact size in bytes.
     */
    @AliasFor(annotation = TestArtifact.class)
    long bytesSize() default 1000000;

    LicenseConfiguration[] licenses() default {};
}
