package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.awaitility.Awaitility.await;
import static org.carlspring.strongbox.util.TestFileUtils.deleteIfExists;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Kate Novik.
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

    private static final String REPOSITORY_SNAPSHOTS = "rmccj-snapshots";

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
    public void testRegenerateArtifactChecksum(@MavenRepository(repositoryId = REPOSITORY_SNAPSHOTS)
                                               Repository repository,
                                               @MavenTestArtifact(repositoryId = REPOSITORY_SNAPSHOTS,
                                                                  id = "org.carlspring.strongbox:strongbox-checksum-one",
                                                                  versions = { "2.0-20190512.202015-1",
                                                                               "2.0-20190512.202101-2",
                                                                               "2.0-20190512.202203-3",
                                                                               "2.0-20190512.202311-4",
                                                                               "2.0-20190512.202601-5" })
                                               Path path)
            throws Exception
    {
        MavenArtifact artifact = createMavenArtifact(STORAGE0,
                                                     REPOSITORY_SNAPSHOTS,
                                                     "org.carlspring.strongbox",
                                                     "strongbox-checksum-one",
                                                     "2.0-20190512.202601-5");

        final String jobName = expectedJobName;

        String artifactPath = getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath() +
                              "/org/carlspring/strongbox/strongbox-checksum-one";

        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                REPOSITORY_SNAPSHOTS,
                                                "org/carlspring/strongbox/strongbox-checksum-one");

        deleteIfExists(new File(artifact.getPath().toString() + ".md5"));
        deleteIfExists(new File(artifact.getPath().toString() + ".sha1"));
        deleteIfExists(new File(artifact.getPath().toString().replaceAll("jar", "pom") + ".md5"));
        deleteIfExists(new File(artifact.getPath().toString().replaceAll("jar", "pom") + ".sha1"));

        deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/maven-metadata.xml.md5"));
        deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/maven-metadata.xml.sha1"));

        deleteIfExists(new File(artifactPath, "/maven-metadata.xml.md5"));
        deleteIfExists(new File(artifactPath, "/maven-metadata.xml.sha1"));

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {

                try
                {
                    assertTrue(new File(artifact.getPath().toString() + ".sha1").exists(),
                               "The checksum file for artifact doesn't exist!");
                    assertTrue(new File(artifact.getPath().toString() + ".sha1").length() > 0,
                               "The checksum file for artifact is empty!");

                    assertTrue(new File(artifact.getPath().toString() + ".md5").exists(),
                               "The checksum file for artifact doesn't exist!");
                    assertTrue(new File(artifact.getPath().toString() + ".md5").length() > 0,
                               "The checksum file for artifact is empty!");

                    assertTrue(new File(artifact.getPath().toString().replaceAll("jar", "pom") + ".sha1").exists(),
                               "The checksum file for pom file doesn't exist!");
                    assertTrue(new File(artifact.getPath().toString().replaceAll("jar", "pom") + ".md5").length() > 0,
                               "The checksum file for pom file is empty!");

                    assertTrue(new File(artifactPath, "/maven-metadata.xml.md5").exists(),
                               "The checksum file for metadata file doesn't exist!");
                    assertTrue(new File(artifactPath, "/maven-metadata.xml.sha1").length() > 0,
                               "The checksum file for metadata file is empty!");
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobName, RegenerateChecksumCronJob.class, STORAGE0,
                         REPOSITORY_SNAPSHOTS,
                         properties ->
                         {
                             properties.put("basePath", "org/carlspring/strongbox/strongbox-checksum-one");
                             properties.put("forceRegeneration", "false");
                         });

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testRegenerateChecksumInRepository(@MavenRepository(repositoryId = REPOSITORY_SNAPSHOTS)
                                                   Repository repository,
                                                   @MavenTestArtifact(repositoryId = REPOSITORY_SNAPSHOTS,
                                                                      id = "org.carlspring.strongbox:strongbox-checksum-two",
                                                                      versions = { "2.0-20190512.202015-1",
                                                                                   "2.0-20190512.202101-2",
                                                                                   "2.0-20190512.202203-3",
                                                                                   "2.0-20190512.202311-4",
                                                                                   "2.0-20190512.202601-5" })
                                                   Path path)
            throws Exception
    {
        MavenArtifact artifact = createMavenArtifact(STORAGE0,
                                                     REPOSITORY_SNAPSHOTS,
                                                     "org.carlspring.strongbox",
                                                     "strongbox-checksum-two",
                                                     "2.0-20190512.202601-5");

        final String jobName = expectedJobName;

        String artifactPath = getRepositoryBasedir(STORAGE0, REPOSITORY_SNAPSHOTS).getAbsolutePath() +
                              "/org/carlspring/strongbox/strongbox-checksum-two";

        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                REPOSITORY_SNAPSHOTS,
                                                "org/carlspring/strongbox/strongbox-checksum-two");

        deleteIfExists(new File(artifact.getPath().toString() + ".md5"));
        deleteIfExists(new File(artifact.getPath().toString() + ".sha1"));
        deleteIfExists(new File(artifact.getPath().toString().replaceAll("jar", "pom") + ".md5"));
        deleteIfExists(new File(artifact.getPath().toString().replaceAll("jar", "pom") + ".sha1"));

        deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/maven-metadata.xml.md5"));
        deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/maven-metadata.xml.sha1"));

        deleteIfExists(new File(artifactPath, "/maven-metadata.xml.md5"));
        deleteIfExists(new File(artifactPath, "/maven-metadata.xml.sha1"));

        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    assertTrue(new File(artifact.getPath().toString() + ".sha1").exists(),
                               "The checksum file for artifact doesn't exist!");
                    assertTrue(new File(artifact.getPath().toString() + ".sha1").length() > 0,
                               "The checksum file for artifact is empty!");

                    assertTrue(new File(artifact.getPath().toString() + ".md5").exists(),
                               "The checksum file for artifact doesn't exist!");
                    assertTrue(new File(artifact.getPath().toString() + ".md5").length() > 0,
                               "The checksum file for artifact is empty!");

                    assertTrue(new File(artifact.getPath().toString().replaceAll("jar", "pom") +
                                        ".sha1").exists(),
                               "The checksum file for pom file doesn't exist!");
                    assertTrue(new File(artifact.getPath().toString().replaceAll("jar", "pom") +
                                        ".sha1").length() > 0,
                               "The checksum file for pom file is empty!");

                    assertTrue(new File(artifactPath, "/maven-metadata.xml.md5").exists(),
                               "The checksum file for metadata file doesn't exist!");
                    assertTrue(new File(artifactPath, "/maven-metadata.xml.sha1").length() > 0,
                               "The checksum file for metadata file is empty!");
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobName,
                         RegenerateChecksumCronJob.class,
                         STORAGE0,
                         REPOSITORY_SNAPSHOTS,
                         properties -> properties.put("forceRegeneration", "false"));

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

}
