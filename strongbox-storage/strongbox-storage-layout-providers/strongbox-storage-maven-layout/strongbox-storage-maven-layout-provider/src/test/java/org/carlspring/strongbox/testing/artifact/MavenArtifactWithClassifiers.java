package org.carlspring.strongbox.testing.artifact;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.maven.model.Repository;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.storage.Storage;
import org.springframework.core.annotation.AliasFor;

/**
 * @author sbespalov
 *
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
@TestArtifact(generator = MavenArtifactGenerator.class, strategy = MavenArtifactWithClassifiersGeneratorStrategy.class)
public @interface MavenArtifactWithClassifiers
{

    /**
     * {@link Storage} ID.
     */
    @AliasFor(annotation = TestArtifact.class)
    String storage() default "storage0";

    /**
     * {@link Repository} ID.
     */
    @AliasFor(annotation = TestArtifact.class)
    String repository() default "";

    /**
     * The artifact "GA" (ex. "org.carlspring.test:test-artifact").
     */
    @AliasFor(annotation = TestArtifact.class)
    String id();

    /**
     * The {@link MavenArtifactCoordinates} versions.
     */
    @AliasFor(annotation = TestArtifact.class)
    String[] versions();

}
