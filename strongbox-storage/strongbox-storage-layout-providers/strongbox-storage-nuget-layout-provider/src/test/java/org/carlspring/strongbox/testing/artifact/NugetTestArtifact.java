package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.NugetArtifactGenerator;
import org.carlspring.strongbox.storage.StorageData;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.maven.model.Repository;
import org.springframework.core.annotation.AliasFor;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Pablo Tirado
 */
@Documented
@Retention(RUNTIME)
@Target({PARAMETER, ANNOTATION_TYPE})
@TestArtifact(generator = NugetArtifactGenerator.class, strategy = NugetArtifactGeneratorStrategy.class)
public @interface NugetTestArtifact
{

    /**
     * {@link StorageData} ID.
     */
    @AliasFor(annotation = TestArtifact.class)
    String storageId() default "storage-nuget";

    /**
     * {@link Repository} ID.
     */
    @AliasFor(annotation = TestArtifact.class)
    String repositoryId() default "";

    /**
     * The artifact identificator (ex. mysql-connector).
     */
    @AliasFor(annotation = TestArtifact.class)
    String id() default "";

    @AliasFor(annotation = TestArtifact.class)
    String resource() default "";    
    
    /**
     * The {@link NugetArtifactCoordinates} versions.
     */
    @AliasFor(annotation = TestArtifact.class)
    String[] versions() default {};

    /**
     * Additional artifact size in bytes.
     */
    @AliasFor(annotation = TestArtifact.class)
    long bytesSize() default 1000000;
    
    /**
     * License Configuration for test artifact
     * @see {https://docs.microsoft.com/en-us/nuget/reference/nuspec#licenseurl}
     */
    LicenseConfiguration[] licenses() default {};
}
