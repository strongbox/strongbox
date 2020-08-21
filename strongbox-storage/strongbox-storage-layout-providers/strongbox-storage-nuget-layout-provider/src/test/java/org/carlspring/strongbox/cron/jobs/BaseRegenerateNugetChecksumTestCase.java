package org.carlspring.strongbox.cron.jobs;

import static java.nio.file.Files.deleteIfExists;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.ThrowingConsumer;

public class BaseRegenerateNugetChecksumTestCase extends BaseCronJobWithNugetIndexingTestCase
{

    protected static final long BYTE_SIZE = 2048;
    protected static final long DEFAULT_BYTE_SIZE = 1000000;

    protected void testRegenerateNugetChecksum(Repository repository,
                                               Path artifactNupkgPath,
                                               String cronJobConfigStorageId,
                                               String cronJobConfigRepositoryId,
                                               Map<String, String> cronJobConfigProperties)
            throws Exception
    {
        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        Path nupkgSha512Path = artifactNupkgPath.resolveSibling(artifactNupkgPath.getFileName() + ".sha512");
        deleteIfExists(nupkgSha512Path);
        assertThat(Files.notExists(nupkgSha512Path))
                .as("The checksum file for nupkg artifact exist!")
                .isTrue();

        NugetArtifactCoordinates coordinates = (NugetArtifactCoordinates) RepositoryFiles.readCoordinates(
                (RepositoryPath) artifactNupkgPath.normalize());
        coordinates.setType("nuspec");
        Path nuspecSha512Path = repositoryPath.resolve(coordinates.getPath() + ".sha512");
        deleteIfExists(nuspecSha512Path);
        assertThat(Files.notExists(nuspecSha512Path))
                .as("The checksum file for nuspec artifact exist!")
                .isTrue();

        List<Path> resultList = new ArrayList<>();
        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) -> {
            if (!StringUtils.equals(jobKey1, jobKey.toString()) || !statusExecuted)
            {
                return;
            }
            resultList.add(nupkgSha512Path);
            resultList.add(nuspecSha512Path);
        });

        addCronJobConfig(jobKey,
                         jobName,
                         RegenerateChecksumCronJob.class,
                         cronJobConfigStorageId,
                         cronJobConfigRepositoryId,
                         properties -> properties.putAll(cronJobConfigProperties));

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());

        assertThat(resultList).hasSize(2);
        resultList.forEach(ThrowingConsumer.unchecked(path -> {
            assertThat(Files.exists(path))
                    .as("The checksum file " + path.toString() + " doesn't exist!")
                    .isTrue();
            assertThat(Files.size(path) > 0)
                    .as("The checksum file is empty!")
                    .isTrue();
        }));

        assertThat(Files.size(artifactNupkgPath)).isBetween(BYTE_SIZE, DEFAULT_BYTE_SIZE);
    }

}
