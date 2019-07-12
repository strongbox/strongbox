package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static java.nio.file.Files.deleteIfExists;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Kate Novik.
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
public class RegenerateMavenChecksumCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String REPOSITORY_SNAPSHOTS1 = "rmccj-snapshots1";
    private static final String REPOSITORY_SNAPSHOTS2 = "rmccj-snapshots2";

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testRegenerateArtifactChecksum(@MavenRepository(repositoryId = REPOSITORY_SNAPSHOTS1)
                                               Repository repository,
                                               @MavenTestArtifact(repositoryId = REPOSITORY_SNAPSHOTS1,
                                                                  id = "org.carlspring.strongbox:strongbox-checksum-one",
                                                                  versions = { "2.0-20190512.202015-1",
                                                                               "2.0-20190512.202101-2",
                                                                               "2.0-20190512.202203-3",
                                                                               "2.0-20190512.202311-4",
                                                                               "2.0-20190512.202601-5" })
                                              List<Path> artifactPaths)
            throws Exception
    {
        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        RepositoryPath artifactPath = (RepositoryPath) artifactPaths.get(4);
        String artifactBaseDir = "org/carlspring/strongbox/strongbox-checksum-one";
        Path artifactBasePath = repositoryPathResolver.resolve(repository, artifactBaseDir);

        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                repository.getId(),
                                                artifactBaseDir);

        Path md5Path = Paths.get( artifactPath.toString() + ".md5");
        deleteIfExists(md5Path);

        Path sha1Path = Paths.get( artifactPath.toString() + ".sha1");
        deleteIfExists(sha1Path);

        Path pomMd5Path = Paths.get(artifactPath.toString().replaceAll("jar", "pom") + ".md5");
        deleteIfExists(pomMd5Path);

        Path pomSha1Path = Paths.get(artifactPath.toString().replaceAll("jar", "pom") + ".sha1");
        deleteIfExists(pomSha1Path);

        Path metadataMd5Path = artifactBasePath.resolve("maven-metadata.xml.md5");
        deleteIfExists(metadataMd5Path);

        Path metadataSha1Path = artifactBasePath.resolve("maven-metadata.xml.sha1");
        deleteIfExists(metadataSha1Path);

        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
            {

                try
                {
                    assertTrue(Files.exists(sha1Path),
                               "The SHA1 checksum file for artifact doesn't exist!");

                    assertTrue(Files.size(sha1Path) > 0,
                               "The SHA1 checksum file for artifact is empty!");

                    assertTrue(Files.exists(md5Path),
                               "The MD5 checksum file for artifact doesn't exist!");
                    assertTrue(Files.size(md5Path) > 0,
                               "The MD5 checksum file for artifact is empty!");

                    assertTrue(Files.exists(pomSha1Path),
                               "The SHA1 checksum file for pom file doesn't exist!");
                    assertTrue(Files.size(pomSha1Path) > 0,
                               "The SHA1 checksum file for pom file is empty!");

                    assertTrue(Files.exists(pomMd5Path),
                               "The MD5 checksum file for pom file doesn't exist!");
                    assertTrue(Files.size(pomMd5Path) > 0,
                               "The MD5 checksum file for pom file is empty!");

                    assertTrue(Files.exists(metadataMd5Path),
                               "The MD5 checksum file for metadata file doesn't exist!");
                    assertTrue(Files.size(metadataMd5Path) > 0,
                               "The MD5 checksum file for metadata file is empty!");

                    assertTrue(Files.exists(metadataSha1Path),
                               "The SHA1 checksum file for metadata file doesn't exist!");
                    assertTrue(Files.size(metadataSha1Path) > 0,
                               "The SHA1 checksum file for metadata file is empty!");
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobKey,
                         jobName,
                         RegenerateChecksumCronJob.class,
                         STORAGE0,
                         repository.getId(),
                         properties ->
                         {
                             properties.put("basePath", artifactBaseDir);
                             properties.put("forceRegeneration", "false");
                         });

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testRegenerateChecksumInRepository(@MavenRepository(repositoryId = REPOSITORY_SNAPSHOTS2)
                                                   Repository repository,
                                                   @MavenTestArtifact(repositoryId = REPOSITORY_SNAPSHOTS2,
                                                                      id = "org.carlspring.strongbox:strongbox-checksum-two",
                                                                      versions = { "2.0-20190512.202015-1",
                                                                                   "2.0-20190512.202101-2",
                                                                                   "2.0-20190512.202203-3",
                                                                                   "2.0-20190512.202311-4",
                                                                                   "2.0-20190512.202601-5" })
                                                  List<Path> artifactPaths)
            throws Exception
    {

        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        RepositoryPath artifactPath = (RepositoryPath) artifactPaths.get(4);
        String artifactBaseDir = "org/carlspring/strongbox/strongbox-checksum-two";
        Path artifactBasePath = repositoryPathResolver.resolve(repository, artifactBaseDir);

        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                repository.getId(),
                                                artifactBaseDir);

        Path md5Path = Paths.get( artifactPath.toString() + ".md5");
        deleteIfExists(md5Path);

        Path sha1Path = Paths.get( artifactPath.toString() + ".sha1");
        deleteIfExists(sha1Path);

        Path pomMd5Path = Paths.get(artifactPath.toString().replaceAll("jar", "pom") + ".md5");
        deleteIfExists(pomMd5Path);

        Path pomSha1Path = Paths.get(artifactPath.toString().replaceAll("jar", "pom") + ".sha1");
        deleteIfExists(pomSha1Path);

        Path metadataMd5Path = artifactBasePath.resolve("maven-metadata.xml.md5");
        deleteIfExists(metadataMd5Path);

        Path metadataSha1Path = artifactBasePath.resolve("maven-metadata.xml.sha1");
        deleteIfExists(metadataSha1Path);

        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
            {
                try
                {
                    assertTrue(Files.exists(sha1Path),
                               "The SHA1 checksum file for artifact doesn't exist!");

                    assertTrue(Files.size(sha1Path) > 0,
                               "The SHA1 checksum file for artifact is empty!");

                    assertTrue(Files.exists(md5Path),
                               "The MD5 checksum file for artifact doesn't exist!");
                    assertTrue(Files.size(md5Path) > 0,
                               "The MD5 checksum file for artifact is empty!");

                    assertTrue(Files.exists(pomSha1Path),
                               "The SHA1 checksum file for pom file doesn't exist!");
                    assertTrue(Files.size(pomSha1Path) > 0,
                               "The SHA1 checksum file for pom file is empty!");

                    assertTrue(Files.exists(pomMd5Path),
                               "The MD5 checksum file for pom file doesn't exist!");
                    assertTrue(Files.size(pomMd5Path) > 0,
                               "The MD5 checksum file for pom file is empty!");

                    assertTrue(Files.exists(metadataMd5Path),
                               "The MD5 checksum file for metadata file doesn't exist!");
                    assertTrue(Files.size(metadataMd5Path) > 0,
                               "The MD5 checksum file for metadata file is empty!");

                    assertTrue(Files.exists(metadataSha1Path),
                               "The SHA1 checksum file for metadata file doesn't exist!");
                    assertTrue(Files.size(metadataSha1Path) > 0,
                               "The SHA1 checksum file for metadata file is empty!");
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobKey,
                         jobName,
                         RegenerateChecksumCronJob.class,
                         STORAGE0,
                         repository.getId(),
                         properties -> properties.put("forceRegeneration", "false"));

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

}
