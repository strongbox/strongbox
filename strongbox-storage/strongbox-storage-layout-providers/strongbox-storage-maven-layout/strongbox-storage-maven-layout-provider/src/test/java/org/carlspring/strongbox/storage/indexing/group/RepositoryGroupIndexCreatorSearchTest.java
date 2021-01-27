package org.carlspring.strongbox.storage.indexing.group;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.indexing.BaseRepositoryIndexCreatorTest;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.Indexer;
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
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;

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
public class RepositoryGroupIndexCreatorSearchTest
        extends BaseRepositoryIndexCreatorTest
{

    private static final String REPOSITORY_RELEASES_0 = "injector-releases-0-rgicst";

    private static final String REPOSITORY_RELEASES_0_1 = "injector-releases-0-1-rgicst";

    private static final String REPOSITORY_RELEASES_0_1_GROUP = "injector-releases-0-1-group-rgicst";

    private static final String REPOSITORY_RELEASES_1 = "injector-releases-1-rgicst";

    private static final String REPOSITORY_RELEASES_1_1 = "injector-releases-1-1-rgicst";

    private static final String REPOSITORY_RELEASES_1_1_GROUP = "injector-releases-1-1-group-rgicst";

    private static final String REPOSITORY_RELEASES_2 = "injector-releases-2-rgicst";

    private static final String REPOSITORY_RELEASES_2_1 = "injector-releases-2-1-rgicst";

    private static final String REPOSITORY_RELEASES_2_1_GROUP = "injector-releases-2-1-group-rgicst";

    private static final String REPOSITORY_RELEASES_3 = "injector-releases-3-rgicst";

    private static final String REPOSITORY_RELEASES_3_1 = "injector-releases-3-1-rgicst";

    private static final String REPOSITORY_RELEASES_3_1_GROUP = "injector-releases-3-1-group-rgicst";

    private static final String REPOSITORY_RELEASES_4 = "injector-releases-4-rgicst";

    private static final String REPOSITORY_RELEASES_4_1 = "injector-releases-4-1-rgicst";

    private static final String REPOSITORY_RELEASES_4_1_GROUP = "injector-releases-4-1-group-rgicst";

    private static final String REPOSITORY_RELEASES_5 = "injector-releases-5-rgicst";

    private static final String REPOSITORY_RELEASES_5_1 = "injector-releases-5-1-rgicst";

    private static final String REPOSITORY_RELEASES_5_1_GROUP = "injector-releases-5-1-group-rgicst";

    private static final String REPOSITORY_RELEASES_6 = "injector-releases-6-rgicst";

    private static final String REPOSITORY_RELEASES_6_1 = "injector-releases-6-1-rgicst";

    private static final String REPOSITORY_RELEASES_6_1_GROUP = "injector-releases-6-1-group-rgicst";

    private static final String REPOSITORY_RELEASES_7 = "injector-releases-7-rgicst";

    private static final String REPOSITORY_RELEASES_7_1 = "injector-releases-7-1-rgicst";

    private static final String REPOSITORY_RELEASES_7_1_GROUP = "injector-releases-7-1-group-rgicst";

    private static final String REPOSITORY_RELEASES_8 = "injector-releases-8-rgicst";

    private static final String REPOSITORY_RELEASES_8_1 = "injector-releases-8-1-rgicst";

    private static final String REPOSITORY_RELEASES_8_1_GROUP = "injector-releases-8-1-group-rgicst";

    private static final String PROPERTIES_INJECTOR_GROUP_ID = "org.carlspring";

    private static final String PROPERTIES_INJECTOR_ARTIFACT_ID = "properties-injector";

    private static final String SLF4J_GROUP_ID = "org.slf4j";

    private static final String SLF4J_ARTIFACT_ID = "slf4j-log4j12";

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

    /**
     * org/slf4j/impl/Log4jLoggerAdapter
     * org/slf4j/impl/Log4jLoggerFactory
     * org/slf4j/impl/Log4jMDCAdapter
     * org/slf4j/impl/StaticLoggerBinder
     * org/slf4j/impl/StaticMarkerBinder
     * org/slf4j/impl/StaticMDCBinder
     * org/slf4j/impl/VersionUtil
     * org/apache/log4j/MDCFriend
     */
    private Resource slf4jJarArtifact = new ClassPathResource("artifacts/slf4j-log4j12-1.7.26.jar");

    @Inject
    private ArtifactManagementService artifactManagementService;

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.HOSTED)
    private RepositoryIndexCreator hostedRepositoryIndexCreator;

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.GROUP)
    private RepositoryIndexCreator groupRepositoryIndexCreator;

    @Inject
    @RepositoryIndexingContextFactoryQualifier(IndexTypeEnum.LOCAL)
    private RepositoryIndexingContextFactory indexingContextFactory;

    private org.apache.maven.index.Indexer indexer = Indexer.INSTANCE;

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void shouldBeCapableToSearchByClassName(@MavenRepository(repositoryId = REPOSITORY_RELEASES_0,
                                                                    setup = MavenIndexedRepositorySetup.class)
                                                   Repository repository,
                                                   @MavenRepository(repositoryId = REPOSITORY_RELEASES_0_1,
                                                                    setup = MavenIndexedRepositorySetup.class)
                                                   Repository repository01,
                                                   @Group(repositories = { REPOSITORY_RELEASES_0,
                                                                           REPOSITORY_RELEASES_0_1 })
                                                   @MavenRepository(repositoryId = REPOSITORY_RELEASES_0_1_GROUP,
                                                                    setup = MavenIndexedRepositorySetup.class)
                                                   Repository groupRepository)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");
        artifactManagementService.validateAndStore(repositoryPath, jarArtifact.getInputStream());
        hostedRepositoryIndexCreator.apply(repository);

        RepositoryPath repository01Path = repositoryPathResolver.resolve(repository01.getStorage().getId(),
                                                                         repository01.getId(),
                                                                         "org/slf4j/slf4j-log4j12/1.7.26/slf4j-log4j12-1.7.26.jar");
        artifactManagementService.validateAndStore(repository01Path, slf4jJarArtifact.getInputStream());
        hostedRepositoryIndexCreator.apply(repository01);

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                groupRepository, groupRepositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);

            q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("Log4jMDCAdapter"));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void deletedArtifactShouldNotExistInNextIndexingContext(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                                                    setup = MavenIndexedRepositorySetup.class)
                                                                   Repository repository,
                                                                   @MavenRepository(repositoryId = REPOSITORY_RELEASES_1_1,
                                                                                    setup = MavenIndexedRepositorySetup.class)
                                                                   Repository repository11,
                                                                   @Group(repositories = { REPOSITORY_RELEASES_1,
                                                                                           REPOSITORY_RELEASES_1_1 })
                                                                   @MavenRepository(repositoryId = REPOSITORY_RELEASES_1_1_GROUP,
                                                                                    setup = MavenIndexedRepositorySetup.class)
                                                                   Repository groupRepository)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");
        artifactManagementService.validateAndStore(repositoryPath, jarArtifact.getInputStream());
        hostedRepositoryIndexCreator.apply(repository);

        RepositoryPath repository01Path = repositoryPathResolver.resolve(repository11.getStorage().getId(),
                                                                         repository11.getId(),
                                                                         "org/slf4j/slf4j-log4j12/1.7.26/slf4j-log4j12-1.7.26.jar");
        artifactManagementService.validateAndStore(repository01Path, slf4jJarArtifact.getInputStream());
        hostedRepositoryIndexCreator.apply(repository11);

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                groupRepository, groupRepositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);

            q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("Log4jMDCAdapter"));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }

        artifactManagementService.delete(repositoryPath, true);
        hostedRepositoryIndexCreator.apply(repository);

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                groupRepository, groupRepositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(0);

            q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("Log4jMDCAdapter"));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }

        repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                        repository.getId(),
                                                        "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");
        artifactManagementService.validateAndStore(repositoryPath, jarArtifact.getInputStream());
        hostedRepositoryIndexCreator.apply(repository);
        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                groupRepository, groupRepositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);

            q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("Log4jMDCAdapter"));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void shouldBeCapableToSearchByFQN(@MavenRepository(repositoryId = REPOSITORY_RELEASES_2,
                                                              setup = MavenIndexedRepositorySetup.class)
                                             Repository repository,
                                             @MavenRepository(repositoryId = REPOSITORY_RELEASES_2_1,
                                                              setup = MavenIndexedRepositorySetup.class)
                                             Repository repository21,
                                             @Group(repositories = { REPOSITORY_RELEASES_2,
                                                                     REPOSITORY_RELEASES_2_1 })
                                             @MavenRepository(repositoryId = REPOSITORY_RELEASES_2_1_GROUP,
                                                              setup = MavenIndexedRepositorySetup.class)
                                             Repository groupRepository)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");
        artifactManagementService.validateAndStore(repositoryPath, jarArtifact.getInputStream());
        hostedRepositoryIndexCreator.apply(repository);

        RepositoryPath repository21Path = repositoryPathResolver.resolve(repository21.getStorage().getId(),
                                                                         repository21.getId(),
                                                                         "org/slf4j/slf4j-log4j12/1.7.26/slf4j-log4j12-1.7.26.jar");
        artifactManagementService.validateAndStore(repository21Path, slf4jJarArtifact.getInputStream());
        hostedRepositoryIndexCreator.apply(repository21);

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                groupRepository, groupRepositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES,
                                             new UserInputSearchExpression("org.carlspring.ioc.PropertyValueInjector"));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);

            q = indexer.constructQuery(MAVEN.CLASSNAMES,
                                       new UserInputSearchExpression("org.slf4j.impl.StaticMarkerBinder"));
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
                                                                         id = PROPERTIES_INJECTOR_GROUP_ID + ":" +
                                                                         PROPERTIES_INJECTOR_ARTIFACT_ID, versions = { "1.8" })
                                                      Path artifactPathPropertiesInjector,
                                                      @MavenRepository(repositoryId = REPOSITORY_RELEASES_3_1,
                                                                       setup = MavenIndexedRepositorySetup.class)
                                                      Repository repository31,
                                                      @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_3_1,
                                                                         id = SLF4J_GROUP_ID + ":" + SLF4J_ARTIFACT_ID,
                                                                         versions = { "1.9" })
                                                      Path artifactPathSlf4j,
                                                      @Group(repositories = { REPOSITORY_RELEASES_3,
                                                                              REPOSITORY_RELEASES_3_1 })
                                                      @MavenRepository(repositoryId = REPOSITORY_RELEASES_3_1_GROUP,
                                                                       setup = MavenIndexedRepositorySetup.class)
                                                      Repository groupRepository)
            throws Exception
    {
        RepositoryPath repositoryPathPropInjSha1 = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                                  repository.getId(),
                                                                                  "org/carlspring/properties-injector/1.8/properties-injector-1.8.jar.sha1");
        String sha1PropInj = Files.readAllLines(repositoryPathPropInjSha1).get(0);
        hostedRepositoryIndexCreator.apply(repository);

        RepositoryPath repositoryPathSlf4jSha1 = repositoryPathResolver.resolve(repository31.getStorage().getId(),
                                                                                repository31.getId(),
                                                                                "org/slf4j/slf4j-log4j12/1.9/slf4j-log4j12-1.9.jar.sha1");
        String sha1Slf4j = Files.readAllLines(repositoryPathSlf4jSha1).get(0);
        hostedRepositoryIndexCreator.apply(repository31);

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                groupRepository, groupRepositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.SHA1, new SourcedSearchExpression(sha1PropInj));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);

            q = indexer.constructQuery(MAVEN.SHA1, new SourcedSearchExpression(sha1Slf4j));
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
                                                                            id = PROPERTIES_INJECTOR_GROUP_ID + ":" + PROPERTIES_INJECTOR_ARTIFACT_ID,
                                                                            classifiers = { "javadoc",
                                                                                            "sources" },
                                                                            versions = { "1.8" })
                                                         Path artifactPathPropertiesInjector,
                                                         @MavenRepository(repositoryId = REPOSITORY_RELEASES_4_1,
                                                                          setup = MavenIndexedRepositorySetup.class)
                                                         Repository repository41,
                                                         @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_4_1,
                                                                            id = SLF4J_GROUP_ID + ":" + SLF4J_ARTIFACT_ID,
                                                                            classifiers = { "javadoc",
                                                                                            "sources" },
                                                                            versions = { "1.9" })
                                                         Path artifactPathSlf4j,
                                                         @Group(repositories = { REPOSITORY_RELEASES_4,
                                                                                 REPOSITORY_RELEASES_4_1 })
                                                         @MavenRepository(repositoryId = REPOSITORY_RELEASES_4_1_GROUP,
                                                                          setup = MavenIndexedRepositorySetup.class)
                                                         Repository groupRepository)
            throws Exception
    {
        RepositoryPath repositoryPathPropInjSha1 = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                                  repository.getId(),
                                                                                  "org/carlspring/properties-injector/1.8/properties-injector-1.8.jar.sha1");
        String sha1PropInj = Files.readAllLines(repositoryPathPropInjSha1).get(0);
        hostedRepositoryIndexCreator.apply(repository);


        RepositoryPath repositoryPathSlf4jSha1 = repositoryPathResolver.resolve(repository41.getStorage().getId(),
                                                                                repository41.getId(),
                                                                                "org/slf4j/slf4j-log4j12/1.9/slf4j-log4j12-1.9.jar.sha1");
        String sha1Slf4j = Files.readAllLines(repositoryPathSlf4jSha1).get(0);
        hostedRepositoryIndexCreator.apply(repository41);

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                groupRepository, groupRepositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.SHA1, new UserInputSearchExpression(sha1PropInj.substring(0, 8)));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);

            q = indexer.constructQuery(MAVEN.SHA1, new UserInputSearchExpression(sha1Slf4j.substring(0, 8)));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void shouldBeCapableToSearchByClassNameFromZippedArtifact(@MavenRepository(repositoryId = REPOSITORY_RELEASES_5,
                                                                                      setup = MavenIndexedRepositorySetup.class)
                                                                     Repository repository,
                                                                     @MavenRepository(repositoryId = REPOSITORY_RELEASES_5_1,
                                                                                      setup = MavenIndexedRepositorySetup.class)
                                                                     Repository repository51,
                                                                     @Group(repositories = { REPOSITORY_RELEASES_5,
                                                                                             REPOSITORY_RELEASES_5_1 })
                                                                     @MavenRepository(repositoryId = REPOSITORY_RELEASES_5_1_GROUP,
                                                                                      setup = MavenIndexedRepositorySetup.class)
                                                                     Repository groupRepository)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip");
        artifactManagementService.validateAndStore(repositoryPath,
                                                   zipArtifact.getInputStream());
        hostedRepositoryIndexCreator.apply(repository);

        RepositoryPath repository51Path = repositoryPathResolver.resolve(repository51.getStorage().getId(),
                                                                         repository51.getId(),
                                                                         "org/slf4j/slf4j-log4j12/1.7.26/slf4j-log4j12-1.7.26.jar");
        artifactManagementService.validateAndStore(repository51Path, slf4jJarArtifact.getInputStream());
        hostedRepositoryIndexCreator.apply(repository51);

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                groupRepository, groupRepositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("PropertiesResources"));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);

            q = indexer.constructQuery(MAVEN.CLASSNAMES, new UserInputSearchExpression("Log4jMDCAdapter"));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);
        }
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void shouldBeCapableToSearchByFQNFromZippedArtifact(@MavenRepository(repositoryId = REPOSITORY_RELEASES_6,
                                                                                setup = MavenIndexedRepositorySetup.class)
                                                               Repository repository,
                                                               @MavenRepository(repositoryId = REPOSITORY_RELEASES_6_1,
                                                                                setup = MavenIndexedRepositorySetup.class)
                                                               Repository repository61,
                                                               @Group(repositories = { REPOSITORY_RELEASES_6,
                                                                                       REPOSITORY_RELEASES_6_1 })
                                                               @MavenRepository(repositoryId = REPOSITORY_RELEASES_6_1_GROUP,
                                                                                setup = MavenIndexedRepositorySetup.class)
                                                               Repository groupRepository)
            throws Exception
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                       repository.getId(),
                                                                       "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip");

        artifactManagementService.validateAndStore(repositoryPath,
                                                   zipArtifact.getInputStream());
        hostedRepositoryIndexCreator.apply(repository);

        RepositoryPath repository61Path = repositoryPathResolver.resolve(repository61.getStorage().getId(),
                                                                         repository61.getId(),
                                                                         "org/slf4j/slf4j-log4j12/1.7.26/slf4j-log4j12-1.7.26.jar");
        artifactManagementService.validateAndStore(repository61Path, slf4jJarArtifact.getInputStream());
        hostedRepositoryIndexCreator.apply(repository61);

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                groupRepository, groupRepositoryIndexCreator, indexingContextFactory))
        {
            Query q = indexer.constructQuery(MAVEN.CLASSNAMES,
                                             new UserInputSearchExpression("org.carlspring.ioc.PropertyValueInjector"));
            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(1);

            q = indexer.constructQuery(MAVEN.CLASSNAMES,
                                       new UserInputSearchExpression("org.slf4j.impl.Log4jLoggerAdapter"));
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
                                                                    id = PROPERTIES_INJECTOR_GROUP_ID + ":" +
                                                                         PROPERTIES_INJECTOR_ARTIFACT_ID,
                                                                    versions = { "1.8" },
                                                                    classifiers = { "javadoc",
                                                                                     "sources" })
                                                 Path artifactPathPropertiesInjector,
                                                 @MavenRepository(repositoryId = REPOSITORY_RELEASES_7_1,
                                                                  setup = MavenIndexedRepositorySetup.class)
                                                 Repository repository71,
                                                 @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_7_1,
                                                                    id = SLF4J_GROUP_ID + ":" + SLF4J_ARTIFACT_ID,
                                                                    versions = { "1.9" },
                                                                    classifiers = { "javadoc",
                                                                                    "sources" })
                                                 Path artifactPathSlf4j,
                                                 @Group(repositories = { REPOSITORY_RELEASES_7,
                                                                         REPOSITORY_RELEASES_7_1 })
                                                 @MavenRepository(repositoryId = REPOSITORY_RELEASES_7_1_GROUP,
                                                                  setup = MavenIndexedRepositorySetup.class)
                                                 Repository groupRepository)
            throws Exception
    {
        hostedRepositoryIndexCreator.apply(repository);
        hostedRepositoryIndexCreator.apply(repository71);

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                groupRepository, groupRepositoryIndexCreator, indexingContextFactory))
        {
            Query groupIdQ = indexer.constructQuery(MAVEN.GROUP_ID, new SourcedSearchExpression("org.carlspring"));
            Query artifactIdQ = indexer.constructQuery(MAVEN.ARTIFACT_ID,
                                                       new SourcedSearchExpression("properties-injector"));

            BooleanQuery q = new BooleanQuery.Builder()
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

            groupIdQ = indexer.constructQuery(MAVEN.GROUP_ID, new SourcedSearchExpression("org.slf4j"));
            artifactIdQ = indexer.constructQuery(MAVEN.ARTIFACT_ID, new SourcedSearchExpression("slf4j-log4j12"));

            q = new BooleanQuery.Builder()
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
                                                                                 id = PROPERTIES_INJECTOR_GROUP_ID + ":" +
                                                                                      PROPERTIES_INJECTOR_ARTIFACT_ID,
                                                                                 versions = { "1.8" },
                                                                                 classifiers = { "javadoc",
                                                                                                 "sources" })
                                                              Path artifactPathPropertiesInjector,
                                                              @MavenRepository(repositoryId = REPOSITORY_RELEASES_8_1,
                                                                               setup = MavenIndexedRepositorySetup.class)
                                                              Repository repository81,
                                                              @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_8_1,
                                                                                 id = SLF4J_GROUP_ID + ":" + SLF4J_ARTIFACT_ID,
                                                                                 versions = { "1.9" },
                                                                                 classifiers = { "javadoc",
                                                                                                 "sources" })
                                                              Path artifactPathSlf4j,
                                                              @Group(repositories = { REPOSITORY_RELEASES_8,
                                                                                      REPOSITORY_RELEASES_8_1 })
                                                              @MavenRepository(repositoryId = REPOSITORY_RELEASES_8_1_GROUP,
                                                                               setup = MavenIndexedRepositorySetup.class)
                                                              Repository groupRepository)
            throws Exception
    {
        hostedRepositoryIndexCreator.apply(repository);
        hostedRepositoryIndexCreator.apply(repository81);

        try (RepositoryIndexingContextAssert repositoryIndexingContextAssert = new RepositoryIndexingContextAssert(
                groupRepository, groupRepositoryIndexCreator, indexingContextFactory))
        {

            Query groupIdQ = indexer.constructQuery(MAVEN.GROUP_ID, new SourcedSearchExpression("org.carlspring"));
            Query artifactIdQ = indexer.constructQuery(MAVEN.ARTIFACT_ID, new SourcedSearchExpression("properties-injector"));

            BooleanQuery q = new BooleanQuery.Builder()
                                             .add(groupIdQ, BooleanClause.Occur.MUST)
                                             .add(artifactIdQ, BooleanClause.Occur.MUST)
                                             .add(indexer.constructQuery(MAVEN.EXTENSION,
                                                                         new SourcedSearchExpression("jar")),
                                                  BooleanClause.Occur.MUST)
                                             .build();

            repositoryIndexingContextAssert.onSearchQuery(q).hitTotalTimes(3);

            groupIdQ = indexer.constructQuery(MAVEN.GROUP_ID, new SourcedSearchExpression("org.slf4j"));
            artifactIdQ = indexer.constructQuery(MAVEN.ARTIFACT_ID, new SourcedSearchExpression("slf4j-log4j12"));

            q = new BooleanQuery.Builder()
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
