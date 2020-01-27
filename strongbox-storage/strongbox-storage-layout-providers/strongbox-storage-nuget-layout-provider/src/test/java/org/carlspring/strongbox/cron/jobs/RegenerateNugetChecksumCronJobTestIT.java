package org.carlspring.strongbox.cron.jobs;

import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.nio.file.Path;
import java.util.Map;

import org.carlspring.strongbox.config.NugetLayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NugetTestArtifact;
import org.carlspring.strongbox.testing.repository.NugetRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Maps;

/**
 * @author Kate Novik.
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = NugetLayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@Execution(CONCURRENT)
public class RegenerateNugetChecksumCronJobTestIT
        extends BaseRegenerateNugetChecksumTestCase
{
    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRegenerateNugetArtifactChecksum(@NugetRepository(storageId = "storage-nuget-rncc-trnac",
                                                                     repositoryId = "repository-rnccjt-trnac")
                                                    Repository repository,
                                                    @NugetTestArtifact(storageId = "storage-nuget-rncc-trnac",
                                                                       repositoryId = "repository-rnccjt-trnac",
                                                                       id = "org.carlspring.strongbox.checksum-second",
                                                                       versions = "1.0.0",
                                                                       bytesSize = BYTE_SIZE)
                                                    Path artifactNupkgPath)
            throws Exception
    {
        Map<String, String> additionalProperties = Maps.newLinkedHashMap();
        additionalProperties.put("basePath", "org.carlspring.strongbox.checksum-second");
        additionalProperties.put("forceRegeneration", "false");

        testRegenerateNugetChecksum(repository,
                                    artifactNupkgPath,
                                    repository.getStorage().getId(),
                                    repository.getId(),
                                    additionalProperties);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                         ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRegenerateNugetChecksumInRepository(@NugetRepository(storageId = "storage-nuget-rncc-trncir",
                                                                         repositoryId = "repository-rnccjt-trncir",
                                                                         policy = RepositoryPolicyEnum.SNAPSHOT)
                                                        Repository repository,
                                                        @NugetTestArtifact(storageId = "storage-nuget-rncc-trncir",
                                                                           repositoryId = "repository-rnccjt-trncir",
                                                                           id = "org.carlspring.strongbox.checksum-one",
                                                                           versions = "1.0.1-alpha",
                                                                           bytesSize = BYTE_SIZE)
                                                        Path artifactNupkgPath)
            throws Exception
    {
        Map<String, String> additionalProperties = Maps.newLinkedHashMap();
        additionalProperties.put("forceRegeneration", "false");

        testRegenerateNugetChecksum(repository,
                                    artifactNupkgPath,
                                    repository.getStorage().getId(),
                                    repository.getId(),
                                    additionalProperties);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRegenerateNugetChecksumInStorage(@NugetRepository(storageId = "storage-nuget-rncc-trncis",
                                                                      repositoryId = "repository-rnccjt-trncis")
                                                     Repository repository,
                                                     @NugetTestArtifact(storageId = "storage-nuget-rncc-trncis",
                                                                        repositoryId = "repository-rnccjt-trncis",
                                                                        id = "org.carlspring.strongbox.checksum-second",
                                                                        versions = "1.0.0",
                                                                        bytesSize = BYTE_SIZE)
                                                     Path artifactNupkgPath)
            throws Exception
    {
        Map<String, String> additionalProperties = Maps.newLinkedHashMap();
        additionalProperties.put("forceRegeneration", "false");

        testRegenerateNugetChecksum(repository,
                                    artifactNupkgPath,
                                    repository.getStorage().getId(),
                                    null,
                                    additionalProperties);
    }

}