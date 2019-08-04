package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithNugetArtifactGeneration;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NugetTestArtifact;
import org.carlspring.strongbox.testing.repository.NugetRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = NugetLayoutProviderTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@Execution(CONCURRENT)
public class NugetArtifactConverterTest
        extends TestCaseWithNugetArtifactGeneration
{

    private static final String REPOSITORY_RELEASES = "nact-releases";

    private static final String ARTIFACT_ID = "Org.Carlspring.Strongbox.Examples.Nuget.Mono";

    private static final String ARTIFACT_VERSION = "1.0";

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testArtifactPathToCoordinatesConversion(@NugetRepository(repositoryId = REPOSITORY_RELEASES)
                                                        Repository repository,
                                                        @NugetTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                           id = ARTIFACT_ID,
                                                                           versions = ARTIFACT_VERSION)
                                                        Path artifactNupkgPath)
    {
        RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        Path relArtifactNupkgPath = repositoryPath.relativize(artifactNupkgPath);
        String nupkgPath = FilenameUtils.separatorsToUnix(relArtifactNupkgPath.toString());
        NugetArtifactCoordinates nac = NugetArtifactCoordinates.parse(nupkgPath);
        assertEquals(ARTIFACT_ID,
                     nac.getId(),
                     "Failed to convert path to artifact coordinates!");
        assertEquals(ARTIFACT_VERSION,
                     nac.getVersion(),
                     "Failed to convert path to artifact coordinates!");

        Path relArtifactNupkgSha512Path = Paths.get(nupkgPath + ".sha512");
        String nupkgSha512Path = FilenameUtils.separatorsToUnix(relArtifactNupkgSha512Path.toString());
        nac = NugetArtifactCoordinates.parse(nupkgSha512Path);
        assertEquals(ARTIFACT_ID,
                     nac.getId(),
                     "Failed to convert path to artifact coordinates!");
        assertEquals(ARTIFACT_VERSION,
                     nac.getVersion(),
                     "Failed to convert path to artifact coordinates!");

        Path relArtifactNuspecPath = Paths.get(nupkgPath.replace("nupkg", "nuspec"));
        String nuspecPath = FilenameUtils.separatorsToUnix(relArtifactNuspecPath.toString());
        nac = NugetArtifactCoordinates.parse(nuspecPath);
        assertEquals(ARTIFACT_ID,
                     nac.getId(),
                     "Failed to convert path to artifact coordinates!");
        assertEquals(ARTIFACT_VERSION,
                     nac.getVersion(),
                     "Failed to convert path to artifact coordinates!");
    }

}
