package org.carlspring.strongbox.io;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.TempRepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author mtodorov
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class ArtifactOutputStreamTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "aos-releases";

    @Inject
    private RepositoryPathResolver repositoryPathResolver;
    
    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    public void testCreateWithTemporaryLocation(@TestRepository(repositoryId = REPOSITORY_RELEASES,
                                                                layout = Maven2LayoutProvider.ALIAS)
                                                Repository repository,
                                                @MavenTestArtifact(id = "org.carlspring.foo:temp-file-test",
                                                                   repositoryId = REPOSITORY_RELEASES,
                                                                   versions = { "1.2.3" })
                                                Path path)
            throws IOException
    {
        RepositoryPath artifactPath = (RepositoryPath) path.normalize();

        TempRepositoryPath artifactPathTemp = RepositoryFiles.temporary(artifactPath);

        LayoutOutputStream afos = (LayoutOutputStream) Files.newOutputStream(artifactPathTemp);

        ByteArrayInputStream bais = new ByteArrayInputStream("This is a test\n".getBytes());
        IOUtils.copy(bais, afos);
        afos.close();

        assertTrue(Files.exists(artifactPathTemp), "Failed to create temporary artifact file!");

        artifactPathTemp.getFileSystem().provider().moveFromTemporaryDirectory(artifactPathTemp);

        assertTrue(Files.exists(artifactPath), "Failed to the move temporary artifact file to original location!");
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class})
    public void testCreateWithTemporaryLocationNoMoveOnClose(@TestRepository(repositoryId = REPOSITORY_RELEASES,
                                                                             layout = Maven2LayoutProvider.ALIAS)
                                                             Repository repository)
            throws IOException
    {
        final Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.foo:temp-file-test:1.2.4:jar");
        final ArtifactCoordinates coordinates = new MavenArtifactCoordinates(artifact);

        RepositoryPath artifactPath = repositoryPathResolver.resolve(repository, coordinates);


        TempRepositoryPath artifactPathTemp = RepositoryFiles.temporary(artifactPath);

        LayoutOutputStream afos = (LayoutOutputStream) Files.newOutputStream(artifactPathTemp);
        ByteArrayInputStream bais = new ByteArrayInputStream("This is a test\n".getBytes());
        IOUtils.copy(bais, afos);
        afos.close();

        assertTrue(Files.exists(artifactPathTemp), "Failed to create temporary artifact file!");

        assertFalse(Files.exists(artifactPath),
                    "Should not have move temporary the artifact file to original location!");
        assertTrue(Files.exists(artifactPathTemp),
                   "Should not have move temporary the artifact file to original location!");
    }

}
