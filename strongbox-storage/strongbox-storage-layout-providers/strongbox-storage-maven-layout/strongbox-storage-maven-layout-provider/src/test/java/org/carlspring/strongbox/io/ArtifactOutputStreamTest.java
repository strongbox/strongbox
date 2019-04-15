package org.carlspring.strongbox.io;

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

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

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

    @Inject
    private RepositoryPathResolver repositoryPathResolver;


    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    public void testCreateWithTemporaryLocation(@TestRepository(repository = "aost=tcwtl-releases",
                                                                layout = Maven2LayoutProvider.ALIAS)
                                                Repository repository,
                                                @MavenTestArtifact(id = "org.carlspring.foo:temp-file-test",
                                                                   repository = "aost-tcwtl-releases",
                                                                   versions = { "1.2.3" })
                                                Path path)
            throws IOException
    {
        ArtifactCoordinates coordinates = RepositoryFiles.readCoordinates((RepositoryPath) path.normalize());

        RepositoryPath artifactPath = repositoryPathResolver.resolve(repository, coordinates);

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
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    public void testCreateWithTemporaryLocationNoMoveOnClose(@TestRepository(repository = "aost-tcwtlnmoc-releases",
                                                                             layout = Maven2LayoutProvider.ALIAS)
                                                             Repository repository,
                                                             @MavenTestArtifact(id = "org.carlspring.foo:temp-file-test",
                                                                                versions = { "1.2.4" })
                                                             Path path)
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
