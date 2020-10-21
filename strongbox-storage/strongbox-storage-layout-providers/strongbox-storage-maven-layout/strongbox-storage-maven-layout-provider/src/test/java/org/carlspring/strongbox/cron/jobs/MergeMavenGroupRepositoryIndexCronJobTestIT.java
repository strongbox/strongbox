package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.indexing.*;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator.RepositoryIndexCreatorQualifier;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexDirectoryPathResolver.RepositoryIndexDirectoryPathResolverQualifier;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;

import javax.inject.Inject;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.maven.index.FlatSearchRequest;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.expr.SourcedSearchExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
class MergeMavenGroupRepositoryIndexCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String REPOSITORY_HOSTED_1 = "merge-index-cron-hosted-1";

    private static final String REPOSITORY_HOSTED_2 = "merge-index-cron-hosted-2";

    private static final String REPOSITORY_GROUP = "merge-index-cron-group";

    private static final String PROPERTIES_INJECTOR_GROUP_ID = "org.carlspring";

    private static final String PROPERTIES_INJECTOR_ARTIFACT_ID = "properties-injector";

    private static final String SLF4J_GROUP_ID = "org.slf4j";

    private static final String SLF4J_ARTIFACT_ID = "slf4j-log4j12";

    private org.apache.maven.index.Indexer indexer = Indexer.INSTANCE;

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.HOSTED)
    private RepositoryIndexCreator hostedRepositoryIndexCreator;

    @Inject
    @RepositoryIndexDirectoryPathResolverQualifier(IndexTypeEnum.LOCAL)
    private RepositoryIndexDirectoryPathResolver repositoryIndexDirectoryPathResolver;

    @Inject
    @RepositoryIndexingContextFactory.RepositoryIndexingContextFactoryQualifier(IndexTypeEnum.LOCAL)
    private RepositoryIndexingContextFactory indexingContextFactory;


    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testMergedIndexesInGroupRepository(@MavenRepository(repositoryId = REPOSITORY_HOSTED_1,
                                                                    setup = MavenIndexedRepositorySetup.class)
                                                   Repository repositoryHosted1,
                                                   @MavenTestArtifact(repositoryId = REPOSITORY_HOSTED_1,
                                                                      id = PROPERTIES_INJECTOR_GROUP_ID + ":" +
                                                                           PROPERTIES_INJECTOR_ARTIFACT_ID,
                                                                      versions = { "1.8" },
                                                                      classifiers = { "javadoc",
                                                                                      "sources" })
                                                   Path artifactPathPropertiesInjector,
                                                   @MavenRepository(repositoryId = REPOSITORY_HOSTED_2,
                                                                    setup = MavenIndexedRepositorySetup.class)
                                                   Repository repositoryHosted2,
                                                   @MavenTestArtifact(repositoryId = REPOSITORY_HOSTED_2,
                                                                      id = SLF4J_GROUP_ID + ":" + SLF4J_ARTIFACT_ID,
                                                                      versions = { "1.9" },
                                                                      classifiers = { "javadoc",
                                                                                      "sources" })
                                                   Path artifactPathSlf4j,
                                                   @Group(repositories = { REPOSITORY_HOSTED_1,
                                                                                          REPOSITORY_HOSTED_2 })
                                                   @MavenRepository(repositoryId = REPOSITORY_GROUP,
                                                                    setup = MavenIndexedRepositorySetup.class)
                                                   Repository groupRepository)
            throws Exception
    {
        final String storageId = groupRepository.getStorage().getId();
        final String repositoryId = groupRepository.getId();

        hostedRepositoryIndexCreator.apply(repositoryHosted1);
        hostedRepositoryIndexCreator.apply(repositoryHosted2);

        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
            {
                try
                {
                    RepositoryPath groupRepositoryIndexPath = repositoryIndexDirectoryPathResolver.resolve(
                            groupRepository);
                    RepositoryPath groupRepositoryPackedIndexPath = groupRepositoryIndexPath.resolve(
                            IndexingContext.INDEX_FILE_PREFIX + ".gz");
                    
                    assertThat(groupRepositoryPackedIndexPath).matches(Files::exists);

                    try (RepositoryCloseableIndexingContext groupRepositoryIndexingContext = indexingContextFactory.create(
                            groupRepository))
                    {
                        groupRepositoryIndexingContext.merge(new SimpleFSDirectory(groupRepositoryIndexPath));

                        Query groupIdQ = indexer.constructQuery(MAVEN.GROUP_ID,
                                                                new SourcedSearchExpression("org.carlspring"));
                        Query artifactIdQ = indexer.constructQuery(MAVEN.ARTIFACT_ID,
                                                                   new SourcedSearchExpression("properties-injector"));

                        BooleanQuery query = new BooleanQuery.Builder()
                                                             .add(groupIdQ, BooleanClause.Occur.MUST)
                                                             .add(artifactIdQ, BooleanClause.Occur.MUST)
                                                             .add(indexer.constructQuery(MAVEN.EXTENSION,
                                                                                         new SourcedSearchExpression("jar")),
                                                                                         BooleanClause.Occur.MUST)
                                                             .build();

                        FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(query,
                                                                                               groupRepositoryIndexingContext));
                        assertThat(response.getTotalHitsCount()).isEqualTo(3);

                        groupIdQ = indexer.constructQuery(MAVEN.GROUP_ID, new SourcedSearchExpression("org.slf4j"));
                        artifactIdQ = indexer.constructQuery(MAVEN.ARTIFACT_ID, new SourcedSearchExpression("slf4j-log4j12"));

                        query = new BooleanQuery.Builder()
                                                .add(groupIdQ, BooleanClause.Occur.MUST)
                                                .add(artifactIdQ, BooleanClause.Occur.MUST)
                                                .add(indexer.constructQuery(MAVEN.EXTENSION,
                                                                            new SourcedSearchExpression("jar")),
                                                                            BooleanClause.Occur.MUST)
                                                .build();

                        response = indexer.searchFlat(new FlatSearchRequest(query, groupRepositoryIndexingContext));
                        assertThat(response.getTotalHitsCount()).isEqualTo(3);
                    }
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobKey,
                         jobName,
                         MergeMavenGroupRepositoryIndexCronJob.class,
                         storageId,
                         repositoryId);

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }
}
