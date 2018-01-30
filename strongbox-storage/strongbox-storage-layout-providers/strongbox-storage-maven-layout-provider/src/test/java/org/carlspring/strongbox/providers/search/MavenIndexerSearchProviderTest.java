package org.carlspring.strongbox.providers.search;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
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
    private MavenIndexerSearchProvider mavenIndexerSearchProvider;

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED));

        return repositories;
    }

    @Before
    public void setUp()
            throws Exception
    {
        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                      true,
                                      "org.carlspring:properties-injector",
                                      "1.8");

        MavenRepositoryConfiguration repositoryConfiguration = new MavenRepositoryConfiguration();
        repositoryConfiguration.setIndexingEnabled(true);
        repositoryConfiguration.setIndexingClassNamesEnabled(false);

        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                      repositoryConfiguration,
                                      "org.carlspring:properties-injector",
                                      "1.8");
    }

    @Test
    public void shouldBeCapableToSearchByClassNameByDefault()
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0, REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar",
                                                   jarArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                  "+classnames:propertiesresources",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.contains(request));
    }

    @Test
    public void shouldBeCapableToSearchByFQNByDefault()
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0, REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar",
                                                   jarArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0, REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                  "+classnames:org +classnames:carlspring +classnames:ioc +classnames:propertyvalueinjector",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.contains(request));
    }

    @Test
    public void shouldBeCapableToSearchByClassNameFromZippedArtifactByDefault()
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0, REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip",
                                                   zipArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                  "+classnames:propertiesresources",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.contains(request));
    }

    @Test
    public void shouldBeCapableToSearchByFQNFromZippedArtifactByDefault()
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0, REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip",
                                                   zipArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0, REPOSITORY_RELEASES_WITH_CLASSNAMES_INDEXED,
                                                  "+classnames:org +classnames:carlspring +classnames:ioc +classnames:propertyvalueinjector",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(mavenIndexerSearchProvider.contains(request));
    }


    @Test
    public void shouldBeAllowedToDisableSearchByClassName()
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0, REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar",
                                                   jarArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                  "+classnames:propertiesresources",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertFalse(mavenIndexerSearchProvider.contains(request));
    }

    @Test
    public void shouldBeAllowedToDisableSearchByFQN()
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0, REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar",
                                                   jarArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0, REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                  "+classnames:org +classnames:carlspring +classnames:ioc +classnames:propertyvalueinjector",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertFalse(mavenIndexerSearchProvider.contains(request));
    }

    @Test
    public void shouldBeAllowedToDisableSearchByClassNameFromZippedArtifact()
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0, REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip",
                                                   zipArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                  "+classnames:propertiesresources",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertFalse(mavenIndexerSearchProvider.contains(request));
    }

    @Test
    public void shouldBeAllowedToDisableSearchByFQNFromZippedArtifact()
            throws Exception
    {
        artifactManagementService.validateAndStore(STORAGE0, REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip",
                                                   zipArtifact.getInputStream());

        SearchRequest request = new SearchRequest(STORAGE0, REPOSITORY_RELEASES_WITHOUT_CLASSNAMES_INDEXED,
                                                  "+classnames:org +classnames:carlspring +classnames:ioc +classnames:propertyvalueinjector",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertFalse(mavenIndexerSearchProvider.contains(request));
    }

    @After
    public void removeRepositories()
            throws Exception
    {
        Set<Repository> repositories = getRepositoriesToClean();
        for (Repository repository : repositories)
        {
            getRepositoryIndexManager().closeIndexersForRepository(repository.getStorage().getId(), repository.getId());
        }
        removeRepositories(getRepositoriesToClean());
        cleanUp();
    }

}
