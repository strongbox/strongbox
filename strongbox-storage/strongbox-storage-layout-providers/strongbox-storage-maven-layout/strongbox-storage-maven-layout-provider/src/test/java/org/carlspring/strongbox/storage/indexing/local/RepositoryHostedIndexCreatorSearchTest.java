package org.carlspring.strongbox.storage.indexing.local;

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

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.expr.SourcedSearchExpression;
import org.apache.maven.index.expr.UserInputSearchExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class RepositoryHostedIndexCreatorSearchTest
        extends BaseRepositoryIndexCreatorTest
{

    private static final String REPOSITORY_RELEASES_1 = "injector-releases-1-rhicst";

    private static final String REPOSITORY_RELEASES_2 = "injector-releases-2-rhicst";

    private static final String REPOSITORY_RELEASES_3 = "injector-releases-3-rhicst";

    private static final String REPOSITORY_RELEASES_4 = "injector-releases-4-rhicst";

    private static final String REPOSITORY_RELEASES_5 = "injector-releases-5-rhicst";

    private static final String REPOSITORY_RELEASES_6 = "injector-releases-6-rhicst";

    private static final String REPOSITORY_RELEASES_7 = "injector-releases-7-rhicst";

    private static final String REPOSITORY_RELEASES_8 = "injector-releases-8-rhicst";

    private static final String GROUP_ID = "org.carlspring";

    private static final String ARTIFACT_ID = "properties-injector";

    /**
     * org/carlspring/ioc/PropertyValueInjector
     * org/carlspring/ioc/InjectionException
     * org/carlspring/ioc/PropertyValue
     * org/carlspring/ioc/PropertiesResources
     */
    private Resource jarArtifact = new ClassPathResource("artifacts/properties-injector-1.7.jar");

    /**
     *
     * org/carlspring/ioc/PropertyValueInjector
     * org/carlspring/ioc/InjectionException
     * org/carlspring/ioc/PropertyValue
     * org/carlspring/ioc/PropertiesResources
     */
    private Resource zipArtifact = new ClassPathResource("artifacts/properties-injector-1.7.zip");

    @Inject
    private ArtifactManagementService artifactManagementService;

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.HOSTED)
    private RepositoryIndexCreator repositoryIndexCreator;

    @Inject
    @RepositoryIndexingContextFactoryQualifier(IndexTypeEnum.LOCAL)
    private RepositoryIndexingContextFactory indexingContextFactory;


    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void shouldBeCapableToSearchByClassName(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                                    setup = MavenIndexedRepositorySetup.class)
                                                   Repository repository)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                       repositoryId,
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");

        artifactManagementService.validateAndStore(repositoryPath, jarArtifact.getInputStream());

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                repository, repositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void deletedArtifactShouldNotExistInNextIndexingContext(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                                                    setup = MavenIndexedRepositorySetup.class)
                                                                   Repository repository)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                       repositoryId,
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");
        artifactManagementService.validateAndStore(repositoryPath, jarArtifact.getInputStream());
        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                repository, repositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }

        repositoryPath = repositoryPathResolver.resolve(storageId,
                                                        repositoryId,
                                                        "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");
        artifactManagementService.delete(repositoryPath, true);
        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                repository, repositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(0);
        }

        repositoryPath = repositoryPathResolver.resolve(storageId,
                                                        repositoryId,
                                                        "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");
        artifactManagementService.validateAndStore(repositoryPath, jarArtifact.getInputStream());
        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                repository, repositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void shouldBeCapableToSearchByFQN(@MavenRepository(repositoryId = REPOSITORY_RELEASES_2,
                                                              setup = MavenIndexedRepositorySetup.class)
                                                              Repository repository)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");

        artifactManagementService.validateAndStore(repositoryPath, jarArtifact.getInputStream());

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                repository, repositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES,
                                             new UserInputSearchExpression("org.carlspring.ioc.PropertyValueInjector"));

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeCapableToSearchByFullSha1Hash(@MavenRepository(repositoryId = REPOSITORY_RELEASES_3,
                                                                       setup = MavenIndexedRepositorySetup.class)
                                                      Repository repository,
                                                      @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_3,
                                                                         id = GROUP_ID + ":" + ARTIFACT_ID,
                                                                         versions = { "1.8" })
                                                      Path artifactPath)
            throws Exception
    {

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.8/properties-injector-1.8.jar.sha1");

        String sha1 = Files.readAllLines(repositoryPath).get(0);

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                repository, repositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.SHA1, new SourcedSearchExpression(sha1));

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeCapableToSearchByPartialSha1Hash(@MavenRepository(repositoryId = REPOSITORY_RELEASES_4,
                                                                          setup = MavenIndexedRepositorySetup.class)
                                                         Repository repository,
                                                         @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_4,
                                                                            id = GROUP_ID + ":" + ARTIFACT_ID,
                                                                            classifiers = { "javadoc",
                                                                                            "sources" },
                                                                            versions = { "1.8" })
                                                         Path artifactPath)
            throws Exception
    {
        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                repository, repositoryIndexCreator, indexingContextFactory))
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                           repository.getId(),
                                                                           "org/carlspring/properties-injector/1.8/properties-injector-1.8.jar.sha1");

            String sha1 = Files.readAllLines(repositoryPath).get(0);
            Query q = indexer.constructQuery(MAVEN.SHA1, new UserInputSearchExpression(sha1.substring(0, 8)));

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void shouldBeCapableToSearchByClassNameFromZippedArtifact(@MavenRepository(repositoryId = REPOSITORY_RELEASES_5,
                                                                                      setup = MavenIndexedRepositorySetup.class)
                                                                     Repository repository)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip");
        artifactManagementService.validateAndStore(repositoryPath,
                                                   zipArtifact.getInputStream());

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                repository, repositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void shouldBeCapableToSearchByFQNFromZippedArtifact(@MavenRepository(repositoryId = REPOSITORY_RELEASES_6,
                                                                                setup = MavenIndexedRepositorySetup.class)
                                                               Repository repository)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip");

        artifactManagementService.validateAndStore(repositoryPath,
                                                   zipArtifact.getInputStream());

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                repository, repositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES,
                                             new UserInputSearchExpression("org.carlspring.ioc.PropertyValueInjector"));

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeCapableToSearchByJavadoc(@MavenRepository(repositoryId = REPOSITORY_RELEASES_7,
                                                                  setup = MavenIndexedRepositorySetup.class)
                                                 Repository repository,
                                                 @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_7,
                                                                    id = GROUP_ID + ":" + ARTIFACT_ID,
                                                                    classifiers = { "javadoc",
                                                                                    "sources" },
                                                                    versions = { "1.8" })
                                                 Path artifactPath)
            throws Exception
    {
        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                repository, repositoryIndexCreator, indexingContextFactory))
        {
            final Query groupIdQ = indexer.constructQuery(MAVEN.GROUP_ID, new SourcedSearchExpression("org.carlspring"));
            final Query artifactIdQ = indexer.constructQuery(MAVEN.ARTIFACT_ID,
                                                             new SourcedSearchExpression("properties-injector"));

            final BooleanQuery q = new BooleanQuery.Builder()
                                                   .add(groupIdQ, BooleanClause.Occur.MUST)
                                                   .add(artifactIdQ, BooleanClause.Occur.MUST)
                                                   .add(indexer.constructQuery(MAVEN.EXTENSION,
                                                                               new SourcedSearchExpression("jar")),
                                                        BooleanClause.Occur.MUST)
                                                   .add(indexer.constructQuery(MAVEN.CLASSIFIER,
                                                                               new SourcedSearchExpression("javadoc")),
                                                        BooleanClause.Occur.MUST)
                                                   .build();

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldBeCapableToSearchAllJarsWithClassifiers(@MavenRepository(repositoryId = REPOSITORY_RELEASES_8,
                                                                               setup = MavenIndexedRepositorySetup.class)
                                                              Repository repository,
                                                              @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_8,
                                                                                 id = GROUP_ID + ":" + ARTIFACT_ID,
                                                                                 classifiers = { "javadoc",
                                                                                                 "sources" },
                                                                                 versions = { "1.8" })
                                                              Path artifactPath)
            throws Exception
    {
        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                repository, repositoryIndexCreator, indexingContextFactory))
        {
            final Query groupIdQ = indexer.constructQuery(MAVEN.GROUP_ID, new SourcedSearchExpression("org.carlspring"));
            final Query artifactIdQ = indexer.constructQuery(MAVEN.ARTIFACT_ID,
                                                             new SourcedSearchExpression("properties-injector"));

            final BooleanQuery q = new BooleanQuery.Builder()
                                                   .add(groupIdQ, BooleanClause.Occur.MUST)
                                                   .add(artifactIdQ, BooleanClause.Occur.MUST)
                                                   .add(indexer.constructQuery(MAVEN.EXTENSION,
                                                                               new SourcedSearchExpression("jar")),
                                                        BooleanClause.Occur.MUST)
                                                   .build();

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(3);
        }
    }

}
