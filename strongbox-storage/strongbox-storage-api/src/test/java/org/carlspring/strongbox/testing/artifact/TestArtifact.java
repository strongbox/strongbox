package org.carlspring.strongbox.testing.artifact;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * This annotation provide ability to inject Artifact {@link Path} instance as
 * test method parameters.
 * <br>
 * The Artifact will also be deployed if {@link Storage} and {@link Repository}
 * provided, in this case {@link RepositoryPath} instance will be injected.
 * <br>
 * If there is no {@link Storage} and {@link Repository} provided then Artifact
 * will be just regular {@link Path} instance located in
 * `{vaultDirectory}/.temp/{testClassName}/{testMethodName}/{artifactURI}`.
 * 
 * @author sbespalov
 *
 */
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TestArtifact
{

    /**
     * {@link Storage} ID.
     */
    String storageId() default "storage0";

    /**
     * {@link Repository} ID.
     */
    String repositoryId() default "";

    /**
     * Layout specific artifact URI (ex.'path/to/artifact.zip').
     */
    String resource() default "";
    
    /**
     * Layout specific {@link ArtifactCoordinates} ID.
     */
    String id() default "";
    
    /**
     * Layout specific {@link ArtifactCoordinates} versions.
     */
    String[] versions() default {};

    /**
     * {@link ArtifactGenerator} class to use.
     */
    Class<? extends ArtifactGenerator> generator();

    /**
     * {@link ArtifactGeneratorStrategy} class to use.
     */
    Class<? extends ArtifactGeneratorStrategy<?>> strategy() default DefaultArtrifactGeneratorStrategy.class;    
    
    /**
     * Artifact size in bytes.
     */
    int size() default 1024;

}
