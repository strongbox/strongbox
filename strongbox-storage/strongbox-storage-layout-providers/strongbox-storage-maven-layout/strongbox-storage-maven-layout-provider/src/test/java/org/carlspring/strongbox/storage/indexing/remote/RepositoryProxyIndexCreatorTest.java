package org.carlspring.strongbox.storage.indexing.remote;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.indexing.BaseRepositoryIndexCreatorTest;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator.RepositoryIndexCreatorQualifier;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexingContextFactory;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexingContextFactory.RepositoryIndexingContextFactoryQualifier;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.lucene.search.Query;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.expr.UserInputSearchExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author carlspring
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class RepositoryProxyIndexCreatorTest
        extends BaseRepositoryIndexCreatorTest
{

    private static final String REPOSITORY_RELEASES = "m2pr-releases";

    private static final String REPOSITORY_RELEASES_2 = "injector-releases-2";

    private static final String PROXY_REPOSITORY_URL =
            "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES + "/";

    private static final String REPOSITORY_PROXY = "m2pr-proxied-releases";

    private static final String PROXY_2_REPOSITORY_URL =
            "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES_2 + "/";

    private static final String REPOSITORY_PROXY_2 = "m2pr-proxied-releases-2";

    private static final String A1 = "org/carlspring/strongbox/strongbox-search-test/1.0/strongbox-search-test-1.0.jar";

    private static final String A2 = "org/carlspring/strongbox/strongbox-search-test/1.1/strongbox-search-test-1.1.jar";

    private static final String A3 = "org/carlspring/strongbox/strongbox-search-test/1.2/strongbox-search-test-1.2.jar";

    /**
     * org/carlspring/ioc/PropertyValueInjector
     * org/carlspring/ioc/InjectionException
     * org/carlspring/ioc/PropertyValue
     * org/carlspring/ioc/PropertiesResources
     */
    private Resource jarArtifact = new ClassPathResource("artifacts/properties-injector-1.7.jar");

    @Inject
    private ArtifactManagementService artifactManagementService;

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.HOSTED)
    private RepositoryIndexCreator hostedRepositoryIndexCreator;

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.PROXY)
    private RepositoryIndexCreator proxyRepositoryIndexCreator;

    @Inject
    @RepositoryIndexingContextFactoryQualifier(IndexTypeEnum.REMOTE)
    private RepositoryIndexingContextFactory remoteIndexingContextFactory;

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testRepositoryIndexFetching(@MavenRepository(repositoryId = REPOSITORY_RELEASES,
                                                             setup = MavenIndexedRepositorySetup.class)
                                            Repository repository,
                                            @MavenRepository(repositoryId = REPOSITORY_PROXY,
                                                             setup = MavenIndexedRepositorySetup.class)
                                            @Remote(url = PROXY_REPOSITORY_URL)
                                            Repository proxyRepository,
                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                               resource = A1)
                                            Path a1,
                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                               resource = A2)
                                            Path a2,
                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                               resource = A3)
                                            Path a3)
            throws IOException
    {
        hostedRepositoryIndexCreator.apply(repository);

        proxyRepositoryIndexCreator.apply(proxyRepository);

        Path indexPropertiesUpdaterFile = repositoryPathResolver.resolve(proxyRepository).resolve(
                ".index/remote/nexus-maven-repository-index-updater.properties");
                
        assertThat(Files.exists(indexPropertiesUpdaterFile))
                .as("Failed to retrieve nexus-maven-repository-index-updater.properties from the remote!")
                .isTrue();
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void deletedArtifactShouldNotExistInNextIndexingContext(@MavenRepository(repositoryId = REPOSITORY_RELEASES_2,
                                                                                    setup = MavenIndexedRepositorySetup.class)
                                                                   Repository repository,
                                                                   @MavenRepository(repositoryId = REPOSITORY_PROXY_2,
                                                                                    setup = MavenIndexedRepositorySetup.class)
                                                                   @Remote(url = PROXY_2_REPOSITORY_URL)
                                                                   Repository proxyRepository)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                       repositoryId,
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");

        artifactManagementService.validateAndStore(repositoryPath, jarArtifact.getInputStream());
        hostedRepositoryIndexCreator.apply(repository);

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                proxyRepository, proxyRepositoryIndexCreator, remoteIndexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }

        artifactManagementService.delete(repositoryPath, true);
        hostedRepositoryIndexCreator.apply(repository);
        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                proxyRepository, proxyRepositoryIndexCreator, remoteIndexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(0);
        }

        repositoryPath = repositoryPathResolver.resolve(storageId,
                                                        repositoryId,
                                                        "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");
        artifactManagementService.validateAndStore(repositoryPath, jarArtifact.getInputStream());
        hostedRepositoryIndexCreator.apply(repository);
        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                proxyRepository, proxyRepositoryIndexCreator, remoteIndexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }
    }

}
