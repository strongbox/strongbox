package org.carlspring.strongbox.providers.search;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.MavenIndexedWithoutClassNamesRepositorySetup;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.lang.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenIndexerSearchProviderTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED = "injector-releases-1";

    private static final String REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED = "injector-releases-2";

    /**
     * org/carlspring/ioc/PropertyValueInjector
     * org/carlspring/ioc/InjectionException
     * org/carlspring/ioc/PropertyValue
     * org/carlspring/ioc/PropertiesResources
     */
    private Resource jarArtifact = new ClassPathResource("artifacts/properties-injector-1.7.jar");

    /**
     * org/carlspring/ioc/PropertyValueInjector
     * org/carlspring/ioc/InjectionException
     * org/carlspring/ioc/PropertyValue
     * org/carlspring/ioc/PropertiesResources
     */
    private Resource zipArtifact = new ClassPathResource("artifacts/properties-injector-1.7.zip");

    @Inject
    private ArtifactManagementService artifactManagementService;

    @Inject
    private Optional<MavenIndexerSearchProvider> mavenIndexerSearchProvider;

    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    @BeforeEach
    public void isIndexingEnabled()
    {
        Assumptions.assumeTrue(mavenIndexerSearchProvider.isPresent());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeCapableToSearchByPartialSha1Checksum(@MavenIndexedWithoutClassNamesRepository(repositoryId = REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED)
                                                             Repository repository,
                                                             @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                                id = "org.carlspring:properties-injector",
                                                                                versions = { "1.8" })
                                                             Path artifactPath)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.8/properties-injector-1.8.jar.sha1");

        String sha1 = Files.readAllLines(repositoryPath).get(0);

        String query = "1:" + sha1.substring(0, 8) + "*";

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repository.getId(),
                                                  query,
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.get().contains(request));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeCapableToSearchByFullSha1Checksum(@MavenIndexedRepository(repositoryId = REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED)
                                                          Repository repository,
                                                          @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                             id = "org.carlspring:properties-injector",
                                                                             versions = { "1.8" })
                                                          Path artifactPath)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.8/properties-injector-1.8.jar.sha1");

        String sha1 = Files.readAllLines(repositoryPath).get(0);

        String query = "1:" + sha1;

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repository.getId(),
                                                  query,
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.get().contains(request));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeCapableToSearchByClassNameByDefault(@MavenIndexedRepository(repositoryId = REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED)
                                                            Repository repository,
                                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                               id = "org.carlspring:properties-injector",
                                                                               versions = { "1.8" })
                                                            Path artifactPath)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");

        artifactManagementService.validateAndStore(repositoryPath,
                                                   jarArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repository.getId(),
                                                  "+classnames:propertiesresources",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.get().contains(request));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeCapableToSearchByFQNByDefault(@MavenIndexedRepository(repositoryId = REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED)
                                                      Repository repository,
                                                      @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                         id = "org.carlspring:properties-injector",
                                                                         versions = { "1.8" })
                                                      Path artifactPath)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");
        artifactManagementService.validateAndStore(repositoryPath,
                                                   jarArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repository.getId(),
                                                  "+classnames:org +classnames:carlspring +classnames:ioc +classnames:propertyvalueinjector",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.get().contains(request));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeCapableToSearchByClassNameFromZippedArtifactByDefault(@MavenIndexedRepository(repositoryId = REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED)
                                                                              Repository repository,
                                                                              @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                                                 id = "org.carlspring:properties-injector",
                                                                                                 versions = { "1.8" })
                                                                              Path artifactPath)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip");

        artifactManagementService.validateAndStore(repositoryPath,
                                                   zipArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repository.getId(),
                                                  "+classnames:propertiesresources",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.get().contains(request));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeCapableToSearchByFQNFromZippedArtifactByDefault(@MavenIndexedRepository(repositoryId = REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED)
                                                                        Repository repository,
                                                                        @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                                           id = "org.carlspring:properties-injector",
                                                                                           versions = { "1.8" })
                                                                        Path artifactPath)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip");

        artifactManagementService.validateAndStore(repositoryPath,
                                                   zipArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repository.getId(),
                                                  "+classnames:org +classnames:carlspring +classnames:ioc +classnames:propertyvalueinjector",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.get().contains(request));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeAllowedToDisableSearchByClassName(@MavenIndexedWithoutClassNamesRepository(repositoryId = REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED)
                                                          Repository repository,
                                                          @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                             id = "org.carlspring:properties-injector",
                                                                             versions = { "1.8" })
                                                          Path artifactPath)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");
        artifactManagementService.validateAndStore(repositoryPath,
                                                   jarArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repository.getId(),
                                                  "+classnames:propertiesresources",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertFalse(mavenIndexerSearchProvider.get().contains(request));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeAllowedToDisableSearchByFQN(@MavenIndexedWithoutClassNamesRepository(repositoryId = REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED)
                                                    Repository repository,
                                                    @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                       id = "org.carlspring:properties-injector",
                                                                       versions = { "1.8" })
                                                    Path artifactPath)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");
        artifactManagementService.validateAndStore(repositoryPath,
                                                   jarArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repository.getId(),
                                                  "+classnames:org +classnames:carlspring +classnames:ioc +classnames:propertyvalueinjector",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertFalse(mavenIndexerSearchProvider.get().contains(request));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeAllowedToDisableSearchByClassNameFromZippedArtifact(@MavenIndexedWithoutClassNamesRepository(repositoryId = REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED)
                                                                            Repository repository,
                                                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                                               id = "org.carlspring:properties-injector",
                                                                                               versions = { "1.8" })
                                                                            Path artifactPath)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip");

        artifactManagementService.validateAndStore(repositoryPath,
                                                   zipArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repository.getId(),
                                                  "+classnames:propertiesresources",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertFalse(mavenIndexerSearchProvider.get().contains(request));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeAllowedToDisableSearchByFQNFromZippedArtifact(@MavenIndexedWithoutClassNamesRepository(repositoryId = REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED)
                                                                      Repository repository,
                                                                      @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                                         id = "org.carlspring:properties-injector",
                                                                                         versions = { "1.8" })
                                                                      Path artifactPath)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0,
                                                                       repository.getId(),
                                                                        "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip");

        artifactManagementService.validateAndStore(repositoryPath,
                                                   zipArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repository.getId(),
                                                  "+classnames:org +classnames:carlspring +classnames:ioc +classnames:propertyvalueinjector",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertFalse(mavenIndexerSearchProvider.get().contains(request));
    }

    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenRepository(setup = MavenIndexedRepositorySetup.class)
    private @interface MavenIndexedRepository
    {
        @AliasFor(annotation = MavenRepository.class)
        String repositoryId() default "";
    }

    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenRepository(setup = MavenIndexedWithoutClassNamesRepositorySetup.class)
    private @interface MavenIndexedWithoutClassNamesRepository
    {
        @AliasFor(annotation = MavenRepository.class)
        String repositoryId() default "";
    }

}
