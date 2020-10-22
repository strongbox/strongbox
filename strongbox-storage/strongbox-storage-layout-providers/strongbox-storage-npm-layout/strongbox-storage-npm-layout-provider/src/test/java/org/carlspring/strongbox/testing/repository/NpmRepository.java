package org.carlspring.strongbox.testing.repository;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
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

@Documented
@Retention(RUNTIME)
@Target({PARAMETER, ANNOTATION_TYPE})
@TestRepository(layout = NpmArtifactCoordinates.LAYOUT_NAME)
public @interface NpmRepository
{

    @AliasFor(annotation = TestRepository.class)
    String storageId() default "storage-npm";

    @AliasFor(annotation = TestRepository.class)
    String repositoryId() default "npm-releases";

    @AliasFor(annotation = TestRepository.class)
    RepositoryPolicyEnum policy() default RepositoryPolicyEnum.RELEASE;

    @AliasFor(annotation = TestRepository.class)
    Class<? extends RepositorySetup>[] setup() default {};

    @AliasFor(annotation = TestRepository.class)
    boolean cleanup() default true;

    @AliasFor(annotation = TestRepository.class)
    String layout() default NpmLayoutProvider.ALIAS;

    @AliasFor(annotation = TestRepository.class)
    String storageProvider() default FileSystemStorageProvider.ALIAS;

}
