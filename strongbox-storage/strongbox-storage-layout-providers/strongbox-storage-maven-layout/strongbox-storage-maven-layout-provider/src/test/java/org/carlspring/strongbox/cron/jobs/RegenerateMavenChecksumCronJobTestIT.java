package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static java.nio.file.Files.deleteIfExists;
import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testRegenerateArtifactChecksum(@MavenRepository(repositoryId = REPOSITORY_SNAPSHOTS1)
                                                       Repository repository,
                                               @MavenTestArtifact(repositoryId = REPOSITORY_SNAPSHOTS1,
                                                                  id = "org.carlspring.strongbox:strongbox-checksum-one",
                                                                  versions = { "2.0-20190512.202015-1" })
                                               Path artifactPath)
            throws Exception
    {
        final String artifactBaseDir = "org/carlspring/strongbox/strongbox-checksum-one";

        Map<String, String> cronJobConfigProperties = Maps.newHashMap();
        cronJobConfigProperties.put("basePath", artifactBaseDir);
        cronJobConfigProperties.put("forceRegeneration", "false");

        testRegenerateChecksum(repository,
                               artifactPath,
                               artifactBaseDir,
                               cronJobConfigProperties);
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testRegenerateChecksumInRepository(@MavenRepository(repositoryId = REPOSITORY_SNAPSHOTS2)
                                                           Repository repository,
                                                   @MavenTestArtifact(repositoryId = REPOSITORY_SNAPSHOTS2,
                                                                      id = "org.carlspring.strongbox:strongbox-checksum-two",
                                                                      versions = { "2.0-20190512.202015-1" })
                                                   Path artifactPath)
            throws Exception
    {
        final String artifactBaseDir = "org/carlspring/strongbox/strongbox-checksum-two";

        Map<String, String> cronJobConfigProperties = Maps.newHashMap();
        cronJobConfigProperties.put("forceRegeneration", "false");

        testRegenerateChecksum(repository,
                               artifactPath,
                               artifactBaseDir,
                               cronJobConfigProperties);
    }

    private void testRegenerateChecksum(Repository repository,
                                        Path artifactPath,
                                        String artifactBaseDir,
                                        Map<String, String> cronJobConfigProperties)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        Path artifactBasePath = repositoryPathResolver.resolve(repository,
                                                               artifactBaseDir);

        artifactMetadataService.rebuildMetadata(storageId,
                                                repositoryId,
                                                artifactBaseDir);

        // JAR MD5 file.
        String fileName = artifactPath.getFileName().toString();
        String checksumFileName = fileName + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path md5Path = artifactPath.resolveSibling(checksumFileName);
        deleteIfExists(md5Path);

        // JAR SHA1 file.
        checksumFileName = fileName + ".sha1";
        Path sha1Path = artifactPath.resolveSibling(checksumFileName);
        deleteIfExists(sha1Path);

        // POM MD5 file.
        String pomFileName = fileName.replace("jar", "pom");
        checksumFileName = pomFileName + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path pomMd5Path = artifactPath.resolveSibling(checksumFileName);
        deleteIfExists(pomMd5Path);

        // POM SHA1 file.
        checksumFileName = pomFileName + ".sha1";
        Path pomSha1Path = artifactPath.resolveSibling(checksumFileName);
        deleteIfExists(pomSha1Path);

        // Metadata XML MD5 file.
        String metadataXmlFileName = "maven-metadata.xml";
        checksumFileName = metadataXmlFileName + "." + MessageDigestAlgorithms.MD5.toLowerCase();
        Path metadataMd5Path = artifactBasePath.resolve(checksumFileName);
        deleteIfExists(metadataMd5Path);

        // Metadata XML SHA1 file.
        checksumFileName = metadataXmlFileName + ".sha1";
        Path metadataSha1Path = artifactBasePath.resolve(checksumFileName);
        deleteIfExists(metadataSha1Path);

        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
            {

                try
                {
                    assertThat(Files.exists(sha1Path))
                            .as("The SHA1 checksum file for artifact doesn't exist!").isTrue();

                    assertThat(Files.size(sha1Path) > 0)
                            .as("The SHA1 checksum file for artifact is empty!").isTrue();

                    assertThat(Files.exists(md5Path))
                            .as("The MD5 checksum file for artifact doesn't exist!").isTrue();
                    assertThat(Files.size(md5Path) > 0)
                            .as("The MD5 checksum file for artifact is empty!").isTrue();

                    assertThat(Files.exists(pomSha1Path))
                            .as("The SHA1 checksum file for pom file doesn't exist!").isTrue();
                    assertThat(Files.size(pomSha1Path) > 0)
                            .as("The SHA1 checksum file for pom file is empty!").isTrue();

                    assertThat(Files.exists(pomMd5Path))
                            .as("The MD5 checksum file for pom file doesn't exist!").isTrue();
                    assertThat(Files.size(pomMd5Path) > 0)
                            .as("The MD5 checksum file for pom file is empty!").isTrue();

                    assertThat(Files.exists(metadataMd5Path))
                            .as("The MD5 checksum file for metadata file doesn't exist!").isTrue();
                    assertThat(Files.size(metadataMd5Path) > 0)
                            .as("The MD5 checksum file for metadata file is empty!").isTrue();

                    assertThat(Files.exists(metadataSha1Path))
                            .as("The SHA1 checksum file for metadata file doesn't exist!").isTrue();
                    assertThat(Files.size(metadataSha1Path) > 0)
                            .as("The SHA1 checksum file for metadata file is empty!").isTrue();
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
                         storageId,
                         repositoryId,
                         properties -> properties.putAll(cronJobConfigProperties));

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

}
