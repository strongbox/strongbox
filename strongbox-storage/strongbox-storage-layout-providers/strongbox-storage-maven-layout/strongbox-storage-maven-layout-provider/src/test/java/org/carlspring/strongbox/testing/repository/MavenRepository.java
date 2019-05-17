package org.carlspring.strongbox.testing.repository;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
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

    /**
     * {@link RepositoryTypeEnum}
     */
    @AliasFor(annotation = TestRepository.class)
    RepositoryTypeEnum type() default RepositoryTypeEnum.HOSTED;

    @AliasFor(annotation = TestRepository.class)
    String implementation() default "file-system";

    @AliasFor(annotation = TestRepository.class)
    String layout() default Maven2LayoutProvider.ALIAS;

    /**
     * {@link RepositoryPolicyEnum}
     */
    @AliasFor(annotation = TestRepository.class)
    RepositoryPolicyEnum policy() default RepositoryPolicyEnum.RELEASE;

    /**
     * {@link RepositoryStatusEnum}
     */
    @AliasFor(annotation = TestRepository.class)
    RepositoryStatusEnum status() default RepositoryStatusEnum.IN_SERVICE;

    @AliasFor(annotation = TestRepository.class)
    long artifactMaxSize() default 0;

    @AliasFor(annotation = TestRepository.class)
    boolean trashEnabled() default true;

    @AliasFor(annotation = TestRepository.class)
    boolean allowsForceDeletion() default false;

    @AliasFor(annotation = TestRepository.class)
    boolean allowsDeployment() default true;

    @AliasFor(annotation = TestRepository.class)
    boolean allowsRedeployment() default false;

    @AliasFor(annotation = TestRepository.class)
    boolean allowsDeletion() default true;

    @AliasFor(annotation = TestRepository.class)
    boolean allowsDirectoryBrowsing() default true;

    @AliasFor(annotation = TestRepository.class)
    boolean checksumHeadersEnabled() default true;

    @AliasFor(annotation = TestRepository.class)
    Class<? extends RepositorySetup>[] setup() default {};

    @AliasFor(annotation = TestRepository.class)
    boolean cleanup() default true;

}
