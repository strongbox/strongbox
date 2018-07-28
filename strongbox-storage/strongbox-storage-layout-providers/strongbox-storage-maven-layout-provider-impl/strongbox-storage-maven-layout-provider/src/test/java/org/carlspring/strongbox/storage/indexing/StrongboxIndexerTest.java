package org.carlspring.strongbox.storage.indexing;

import static org.junit.Assert.assertThat;

import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.apache.lucene.search.Query;
import org.apache.maven.index.FlatSearchRequest;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.expr.SourcedSearchExpression;
import org.apache.maven.index.expr.UserInputSearchExpression;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.util.IndexContextHelper;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class StrongboxIndexerTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES_1 = "injector-releases-1";

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
    private Optional<Indexer> indexer;

    @Before
    public void isIndexingEnabled()
    {
        Assume.assumeTrue(repositoryIndexManager.isPresent());
    }

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Before
    public void setUp()
            throws Exception
    {
        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES_1,
                                      true,
                                      "org.carlspring:properties-injector",
                                      "1.8");


    }

    @Test
    public void indexerShouldBeCapableToSearchByClassName()
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();

        artifactManagementService.validateAndStore(STORAGE0, REPOSITORY_RELEASES_1,
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar",
                                                   jarArtifact.getInputStream());

        String contextId = IndexContextHelper.getContextId(STORAGE0, REPOSITORY_RELEASES_1,
                                                           IndexTypeEnum.LOCAL.getType());
        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);
        Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(1));
    }

    @Test
    public void indexerShouldBeCapableToSearchByFQN()
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();

        artifactManagementService.validateAndStore(STORAGE0, REPOSITORY_RELEASES_1,
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar",
                                                   jarArtifact.getInputStream());

        String contextId = IndexContextHelper.getContextId(STORAGE0, REPOSITORY_RELEASES_1,
                                                           IndexTypeEnum.LOCAL.getType());
        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);
        Query q = indexer.constructQuery(MAVEN.CLASSNAMES,
                                         new UserInputSearchExpression("org.carlspring.ioc.PropertyValueInjector"));

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(1));
    }

    @Test
    public void indexerShouldBeCapableToSearchByFullSha1Hash()
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();

        String sha1 = Files.readAllLines(getVaultDirectoryPath()
                                                 .resolve("storages")
                                                 .resolve(STORAGE0)
                                                 .resolve(REPOSITORY_RELEASES_1)
                                                 .resolve("org")
                                                 .resolve("carlspring")
                                                 .resolve("properties-injector")
                                                 .resolve("1.8")
                                                 .resolve("properties-injector-1.8.jar.sha1")
                                                 .toAbsolutePath()).get(0);

        String contextId = IndexContextHelper.getContextId(STORAGE0, REPOSITORY_RELEASES_1,
                                                           IndexTypeEnum.LOCAL.getType());
        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);
        Query q = indexer.constructQuery(MAVEN.SHA1, new SourcedSearchExpression(sha1));

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(1));
    }

    @Test
    public void indexerShouldBeCapableToSearchByPartialSha1Hash()
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();

        String sha1 = Files.readAllLines(getVaultDirectoryPath()
                                                 .resolve("storages")
                                                 .resolve(STORAGE0)
                                                 .resolve(REPOSITORY_RELEASES_1)
                                                 .resolve("org")
                                                 .resolve("carlspring")
                                                 .resolve("properties-injector")
                                                 .resolve("1.8")
                                                 .resolve("properties-injector-1.8.jar.sha1")
                                                 .toAbsolutePath()).get(0);

        String contextId = IndexContextHelper.getContextId(STORAGE0, REPOSITORY_RELEASES_1,
                                                           IndexTypeEnum.LOCAL.getType());
        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);
        Query q = indexer.constructQuery(MAVEN.SHA1,
                                         new UserInputSearchExpression(sha1.substring(0, 8)));

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(1));
    }

    @Test
    public void indexerShouldBeCapableToSearchByClassNameFromZippedArtifact()
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();

        artifactManagementService.validateAndStore(STORAGE0, REPOSITORY_RELEASES_1,
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip",
                                                   zipArtifact.getInputStream());

        String contextId = IndexContextHelper.getContextId(STORAGE0, REPOSITORY_RELEASES_1,
                                                           IndexTypeEnum.LOCAL.getType());
        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);
        Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(1));
    }

    @Test
    public void indexerShouldBeCapableToSearchByFQNFromZippedArtifact()
            throws Exception
    {
        Indexer indexer = this.indexer.get();
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();

        artifactManagementService.validateAndStore(STORAGE0, REPOSITORY_RELEASES_1,
                                                   "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip",
                                                   zipArtifact.getInputStream());

        String contextId = IndexContextHelper.getContextId(STORAGE0, REPOSITORY_RELEASES_1,
                                                           IndexTypeEnum.LOCAL.getType());
        RepositoryIndexer ri = repositoryIndexManager.getRepositoryIndexer(contextId);
        Query q = indexer.constructQuery(MAVEN.CLASSNAMES,
                                         new UserInputSearchExpression("org.carlspring.ioc.PropertyValueInjector"));

        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q, ri.getIndexingContext()));

        assertThat(response.getTotalHitsCount(), CoreMatchers.equalTo(1));
    }

    @After
    public void removeRepositories()
            throws Exception
    {
        closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES_1);
        removeRepositories(getRepositoriesToClean());
        cleanUp();
    }

}
