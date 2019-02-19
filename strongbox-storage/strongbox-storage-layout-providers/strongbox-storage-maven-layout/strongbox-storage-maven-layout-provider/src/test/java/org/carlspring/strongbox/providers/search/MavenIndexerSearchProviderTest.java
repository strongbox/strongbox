package org.carlspring.strongbox.providers.search;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;

import javax.inject.Inject;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
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

    @BeforeEach
    public void isIndexingEnabled()
    {
        Assumptions.assumeTrue(mavenIndexerSearchProvider.isPresent());
    }

    @BeforeEach
    public void setUp(TestInfo testInfo)
            throws Exception
    {
        createRepositoryWithArtifacts(STORAGE0,
                                      getRepositoryName(REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED, testInfo),
                                      true,
                                      "org.carlspring:properties-injector",
                                      "1.8");

        MutableMavenRepositoryConfiguration repositoryConfiguration = new MutableMavenRepositoryConfiguration();
        repositoryConfiguration.setIndexingEnabled(true);
        repositoryConfiguration.setIndexingClassNamesEnabled(false);

        createRepositoryWithArtifacts(STORAGE0,
                                      getRepositoryName(REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED, testInfo),
                                      repositoryConfiguration,
                                      "org.carlspring:properties-injector",
                                      "1.8");
    }

    @Test
    public void shouldBeCapableToSearchByPartialSha1Checksum(TestInfo testInfo)
            throws Exception
    {
        String sha1 = Files.readAllLines(getVaultDirectoryPath()
                                                 .resolve("storages")
                                                 .resolve(STORAGE0)
                                                 .resolve(getRepositoryName(
                                                         REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED, testInfo))
                                                 .resolve("org")
                                                 .resolve("carlspring")
                                                 .resolve("properties-injector")
                                                 .resolve("1.8")
                                                 .resolve("properties-injector-1.8.jar.sha1")
                                                 .toAbsolutePath()).get(0);

        String query = "1:" + sha1.substring(0, 8) + "*";

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  getRepositoryName(REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                    testInfo),
                                                  query,
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.get().contains(request));
    }


    @Test
    public void shouldBeCapableToSearchByFullSha1Checksum(TestInfo testInfo)
            throws Exception
    {
        String sha1 = Files.readAllLines(getVaultDirectoryPath()
                                                 .resolve("storages")
                                                 .resolve(STORAGE0)
                                                 .resolve(getRepositoryName(REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                            testInfo))
                                                 .resolve("org")
                                                 .resolve("carlspring")
                                                 .resolve("properties-injector")
                                                 .resolve("1.8")
                                                 .resolve("properties-injector-1.8.jar.sha1")
                                                 .toAbsolutePath()).get(0);

        String query = "1:" + sha1;

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  getRepositoryName(REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                    testInfo),
                                                  query,
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.get().contains(request));
    }

    @Test
    public void shouldBeCapableToSearchByClassNameByDefault(TestInfo testInfo)
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0,
                                                   getRepositoryName(REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                     testInfo),
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar",
                                                   jarArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  getRepositoryName(REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                    testInfo),
                                                  "+classnames:propertiesresources",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.get().contains(request));
    }

    @Test
    public void shouldBeCapableToSearchByFQNByDefault(TestInfo testInfo)
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0,
                                                   getRepositoryName(REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                     testInfo),
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar",
                                                   jarArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  getRepositoryName(REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                    testInfo),
                                                  "+classnames:org +classnames:carlspring +classnames:ioc +classnames:propertyvalueinjector",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.get().contains(request));
    }

    @Test
    public void shouldBeCapableToSearchByClassNameFromZippedArtifactByDefault(TestInfo testInfo)
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0,
                                                   getRepositoryName(REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                     testInfo),
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip",
                                                   zipArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  getRepositoryName(REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                    testInfo),
                                                  "+classnames:propertiesresources",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.get().contains(request));
    }

    @Test
    public void shouldBeCapableToSearchByFQNFromZippedArtifactByDefault(TestInfo testInfo)
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0,
                                                   getRepositoryName(REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                     testInfo),
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip",
                                                   zipArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  getRepositoryName(REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                                    testInfo),
                                                  "+classnames:org +classnames:carlspring +classnames:ioc +classnames:propertyvalueinjector",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.get().contains(request));
    }


    @Test
    public void shouldBeAllowedToDisableSearchByClassName(TestInfo testInfo)
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0,
                                                   getRepositoryName(REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                     testInfo),
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar",
                                                   jarArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  getRepositoryName(REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                    testInfo),
                                                  "+classnames:propertiesresources",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertFalse(mavenIndexerSearchProvider.get().contains(request));
    }

    @Test
    public void shouldBeAllowedToDisableSearchByFQN(TestInfo testInfo)
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0,
                                                   getRepositoryName(REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                     testInfo),
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar",
                                                   jarArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  getRepositoryName(REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                    testInfo),
                                                  "+classnames:org +classnames:carlspring +classnames:ioc +classnames:propertyvalueinjector",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertFalse(mavenIndexerSearchProvider.get().contains(request));
    }

    @Test
    public void shouldBeAllowedToDisableSearchByClassNameFromZippedArtifact(TestInfo testInfo)
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0,
                                                   getRepositoryName(REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                     testInfo),
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip",
                                                   zipArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  getRepositoryName(REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                    testInfo),
                                                  "+classnames:propertiesresources",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertFalse(mavenIndexerSearchProvider.get().contains(request));
    }

    @Test
    public void shouldBeAllowedToDisableSearchByFQNFromZippedArtifact(TestInfo testInfo)
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0,
                                                   getRepositoryName(REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                     testInfo),
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip",
                                                   zipArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  getRepositoryName(REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                    testInfo),
                                                  "+classnames:org +classnames:carlspring +classnames:ioc +classnames:propertyvalueinjector",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertFalse(mavenIndexerSearchProvider.get().contains(request));
    }

    private Set<MutableRepository> getRepositories(TestInfo testInfo)
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED, testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                                testInfo),
                                              Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @AfterEach
    public void removeRepositories(TestInfo testInfo)
            throws Exception
    {
        removeRepositories(getRepositories(testInfo));
    }

}
