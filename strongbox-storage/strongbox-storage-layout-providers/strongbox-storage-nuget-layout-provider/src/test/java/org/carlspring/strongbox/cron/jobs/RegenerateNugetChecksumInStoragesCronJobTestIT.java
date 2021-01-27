package org.carlspring.strongbox.cron.jobs;

import static org.carlspring.strongbox.cron.jobs.BaseRegenerateNugetChecksumTestCase.BYTE_SIZE;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.nio.file.Path;
import java.util.Map;

import org.carlspring.strongbox.config.NugetLayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.config.hazelcast.HazelcastConfiguration;
import org.carlspring.strongbox.config.hazelcast.HazelcastInstanceId;
import org.carlspring.strongbox.storage.repository.Repository;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Maps;

/**
 * @author Kate Novik.
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = NugetLayoutProviderCronTasksTestConfig.class)
@SpringBootTest(properties = { "strongbox.vault=${strongbox.basedir}/strongbox-vault-rnccjt" })
@ActiveProfiles(profiles = {"test", "RegenerateNugetChecksumInStoragesCronJobTestConfig"})
@Execution(CONCURRENT)
public class RegenerateNugetChecksumInStoragesCronJobTestIT
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
    public void testRegenerateNugetChecksumInStorages(@NugetRepository(storageId = "storage-nuget-rncc-trncis2",
                                                                       repositoryId = "repository-rnccjt-trncis2")
                                                      Repository repository,
                                                      @NugetTestArtifact(storageId = "storage-nuget-rncc-trncis2",
                                                                         repositoryId = "repository-rnccjt-trncis2",
                                                                         id = "org.carlspring.strongbox.checksum-one",
                                                                         versions = "1.0.0",
                                                                         bytesSize = BYTE_SIZE)
                                                      Path artifactNupkgPath)
            throws Exception
    {
        Map<String, String> additionalProperties = Maps.newLinkedHashMap();
        additionalProperties.put("forceRegeneration", "false");

        testRegenerateNugetChecksum(repository,
                                    artifactNupkgPath,
                                    null,
                                    null,
                                    additionalProperties);
    }

    @Profile("RegenerateNugetChecksumInStoragesCronJobTestConfig")
    @Import(HazelcastConfiguration.class)
    @Configuration
    public static class RegenerateNugetChecksumInStoragesCronJobTestConfig
    {

        @Primary
        @Bean
        public HazelcastInstanceId hazelcastInstanceIdRnccjt()
        {
            return new HazelcastInstanceId("RegenerateNugetChecksumInStoragesCronJobTestIT-hazelcast-instance");
        }

    }
}
