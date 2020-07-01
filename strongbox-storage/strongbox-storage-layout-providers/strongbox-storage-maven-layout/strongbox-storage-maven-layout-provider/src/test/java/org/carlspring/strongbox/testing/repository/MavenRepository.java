package org.carlspring.strongbox.testing.repository;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.storage.FileSystemStorageProvider;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.storage.repository.RepositorySetup;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author sbespalov
 */
@Documented
@Retention(RUNTIME)
@Target({PARAMETER, ANNOTATION_TYPE})
@TestRepository(layout = MavenArtifactCoordinates.LAYOUT_NAME)
public @interface MavenRepository
{

    @AliasFor(annotation = TestRepository.class)
    String storageId() default "storage0";

    @AliasFor(annotation = TestRepository.class)
    String repositoryId() default "releases";

    @AliasFor(annotation = TestRepository.class)
    RepositoryPolicyEnum policy() default RepositoryPolicyEnum.RELEASE;

    @AliasFor(annotation = TestRepository.class)
    Class<? extends RepositorySetup>[] setup() default {};

    @AliasFor(annotation = TestRepository.class)
    boolean cleanup() default true;

    @AliasFor(annotation = TestRepository.class)
    String layout() default Maven2LayoutProvider.ALIAS;

    @AliasFor(annotation = TestRepository.class)
    String storageProvider() default FileSystemStorageProvider.ALIAS;

}
