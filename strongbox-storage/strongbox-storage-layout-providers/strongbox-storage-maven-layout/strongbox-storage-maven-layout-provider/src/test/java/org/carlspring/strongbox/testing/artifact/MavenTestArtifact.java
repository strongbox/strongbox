package org.carlspring.strongbox.testing.artifact;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
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
@Target({PARAMETER, ANNOTATION_TYPE})
@TestArtifact(generator = MavenArtifactGenerator.class, strategy = MavenArtifactGeneratorStrategy.class)
public @interface MavenTestArtifact
{

    /**
     * {@link Storage} ID.
     */
    @AliasFor(annotation = TestArtifact.class)
    String storageId() default "storage0";

    /**
     * {@link Repository} ID.
     */
    @AliasFor(annotation = TestArtifact.class)
    String repositoryId() default "";

    /**
     * The artifact "GA" (ex. "org.carlspring.test:test-artifact").
     */
    @AliasFor(annotation = TestArtifact.class)
    String id() default "";

    /**
     * The {@link MavenArtifactCoordinates} versions.
     */
    @AliasFor(annotation = TestArtifact.class)
    String[] versions() default {};
    
    /**
     * Maven classifiers ("javadoc","sources", etc.).
     */
    String[] classifiers() default {};
    
    /**
     * Maven artifact packaging.
     * <br>
     * <br>
     * <b>TODO:</b> only "maven-plugin" currently supported besides default "jar" packaging
     */
    String packaging() default "jar";

}
