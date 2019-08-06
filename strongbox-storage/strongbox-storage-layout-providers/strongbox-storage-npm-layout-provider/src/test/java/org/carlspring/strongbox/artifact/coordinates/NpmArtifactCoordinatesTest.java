package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.config.NpmLayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.NpmRepositoryTestCase;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NpmTestArtifact;
import org.carlspring.strongbox.testing.repository.NpmRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author sbespalov
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = NpmLayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@Execution(CONCURRENT)
public class NpmArtifactCoordinatesTest
        extends NpmRepositoryTestCase
{
    private static final String REPOSITORY_RELEASES = "npm-act-releases";

    private static final String ARTIFACT_1_ID = "react-redux";

    private static final String ARTIFACT_1_VERSION = "5.0.6";

    private static final String ARTIFACT_2_ID = "node";

    private static final String ARTIFACT_2_VERSION = "8.0.51";

    private static final String ARTIFACT_2_SCOPE = "@types";

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    void testArtifactPathToCoordinatesConversion(@NpmRepository(repositoryId = REPOSITORY_RELEASES)
                                                 Repository repository,
                                                 @NpmTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                  id = ARTIFACT_1_ID,
                                                                  versions = ARTIFACT_1_VERSION)
                                                 Path artifact1Path,
                                                 @NpmTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                  id = ARTIFACT_2_ID,
                                                                  versions = ARTIFACT_2_VERSION,
                                                                  scope = ARTIFACT_2_SCOPE)
                                                 Path artifact2Path)
    {
        RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        Path relArtifact1Path = repositoryPath.relativize(artifact1Path);
        String artifact1PathStr = FilenameUtils.separatorsToUnix(relArtifact1Path.toString());
        NpmArtifactCoordinates coordinates = NpmArtifactCoordinates.parse(artifact1PathStr);

        assertNull(coordinates.getScope());
        assertEquals(ARTIFACT_1_ID, coordinates.getName());
        assertEquals(ARTIFACT_1_VERSION, coordinates.getVersion());
        assertEquals("tgz", coordinates.getExtension());

        Path relArtifact2Path = repositoryPath.relativize(artifact2Path);
        String artifact2PathStr = FilenameUtils.separatorsToUnix(relArtifact2Path.toString());
        coordinates = NpmArtifactCoordinates.parse(artifact2PathStr);

        assertEquals(ARTIFACT_2_SCOPE, coordinates.getScope());
        assertEquals(ARTIFACT_2_ID, coordinates.getName());
        assertEquals(ARTIFACT_2_VERSION, coordinates.getVersion());
        assertEquals("tgz", coordinates.getExtension());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    void testMetadataPathToCoordinatesConversion(@NpmRepository(repositoryId = REPOSITORY_RELEASES)
                                                 Repository repository,
                                                 @NpmTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                  id = ARTIFACT_1_ID,
                                                                  versions = ARTIFACT_1_VERSION)
                                                 Path artifact1Path,
                                                 @NpmTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                  id = ARTIFACT_2_ID,
                                                                  versions = ARTIFACT_2_VERSION,
                                                                  scope = ARTIFACT_2_SCOPE)
                                                 Path artifact2Path)
    {
        RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        Path relArtifact1Path = repositoryPath.relativize(artifact1Path).resolveSibling("package.json");
        String artifact1PathStr = FilenameUtils.separatorsToUnix(relArtifact1Path.toString());
        NpmArtifactCoordinates coordinates = NpmArtifactCoordinates.parse(artifact1PathStr);

        assertNull(coordinates.getScope());
        assertEquals(ARTIFACT_1_ID, coordinates.getName());
        assertEquals(ARTIFACT_1_VERSION, coordinates.getVersion());
        assertEquals("json", coordinates.getExtension());

        Path relArtifact2Path = repositoryPath.relativize(artifact2Path).resolveSibling("package.json");
        String artifact2PathStr = FilenameUtils.separatorsToUnix(relArtifact2Path.toString());
        coordinates = NpmArtifactCoordinates.parse(artifact2PathStr);

        assertEquals(ARTIFACT_2_SCOPE, coordinates.getScope());
        assertEquals(ARTIFACT_2_ID, coordinates.getName());
        assertEquals(ARTIFACT_2_VERSION, coordinates.getVersion());
        assertEquals("json", coordinates.getExtension());
    }

    @Test
    void testVersionAssertion()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> NpmArtifactCoordinates.parse("@types/node/8.beta1/node-8.beta1.tgz"));
    }

    @Test
    void testNameAssertion()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> NpmArtifactCoordinates.parse("@types/_node/8.0.51/node-8.0.51.tgz"));
    }
}
