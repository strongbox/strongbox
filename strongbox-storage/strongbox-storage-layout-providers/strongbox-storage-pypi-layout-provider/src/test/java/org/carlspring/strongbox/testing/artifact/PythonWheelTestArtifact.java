package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.generator.PythonWheelArtifactGenerator;
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
@TestArtifact(generator = PythonWheelArtifactGenerator.class, strategy = PythonWheelArtifactGeneratorStrategy.class)
public @interface PythonWheelTestArtifact
{
    /**
     * {@link Storage} ID.
     */
    @AliasFor(annotation = TestArtifact.class)
    String storageId() default "storage1";

    /**
     * {@link Repository} ID.1
     */
    @AliasFor(annotation = TestArtifact.class)
    String repositoryId() default "";

    /**
     * The artifact distribution (ex. mysql-connector).
     */
    @AliasFor(annotation = TestArtifact.class)
    String id() default "";

    /**
     * The {@link org.carlspring.strongbox.artifact.coordinates.PypiWheelArtifactCoordinates} versions.
     */
    @AliasFor(annotation = TestArtifact.class)
    String[] versions() default {};
}
