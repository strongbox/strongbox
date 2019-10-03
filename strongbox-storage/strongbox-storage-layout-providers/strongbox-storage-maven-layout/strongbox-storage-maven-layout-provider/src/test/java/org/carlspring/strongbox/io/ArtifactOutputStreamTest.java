package org.carlspring.strongbox.io;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.TempRepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class ArtifactOutputStreamTest
{

    private static final String REPOSITORY_RELEASES = "aos-releases";

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testCreateWithTemporaryLocation(@MavenRepository(repositoryId = REPOSITORY_RELEASES)
                                                Repository repository,
                                                @MavenTestArtifact(id = "org.carlspring.foo:temp-file-test",
                                                                   repositoryId = REPOSITORY_RELEASES,
                                                                   versions = { "1.2.3" })
                                                Path path)
            throws IOException
    {
        RepositoryPath artifactPath = (RepositoryPath) path.normalize();

        TempRepositoryPath artifactPathTemp = RepositoryFiles.temporary(artifactPath);

        OutputStream afos = Files.newOutputStream(artifactPathTemp);

        ByteArrayInputStream bais = new ByteArrayInputStream("This is a test\n".getBytes());
        IOUtils.copy(bais, afos);
        afos.close();

        assertThat(Files.exists(artifactPathTemp)).as("Failed to create temporary artifact file!").isTrue();

        artifactPathTemp.getFileSystem().provider().moveFromTemporaryDirectory(artifactPathTemp);

        assertThat(Files.exists(artifactPath)).as("Failed to the move temporary artifact file to original location!").isTrue();
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class})
    public void testCreateWithTemporaryLocationNoMoveOnClose(@MavenRepository(repositoryId = REPOSITORY_RELEASES)
                                                             Repository repository)
            throws IOException
    {
        final MavenArtifact artifact = MavenArtifactTestUtils.getArtifactFromGAVTC("org.carlspring.foo:temp-file-test:1.2.4:jar");
        final ArtifactCoordinates coordinates = new MavenArtifactCoordinates(artifact);

        RepositoryPath artifactPath = repositoryPathResolver.resolve(repository, coordinates);


        TempRepositoryPath artifactPathTemp = RepositoryFiles.temporary(artifactPath);

        OutputStream afos = Files.newOutputStream(artifactPathTemp);
        ByteArrayInputStream bais = new ByteArrayInputStream("This is a test\n".getBytes());
        IOUtils.copy(bais, afos);
        afos.close();

        assertThat(Files.exists(artifactPathTemp)).as("Failed to create temporary gav file!").isTrue();

        assertThat(Files.exists(artifactPath))
                .as("Should not have move temporary the gav file to original location!")
                .isFalse();
        assertThat(Files.exists(artifactPathTemp))
                .as("Should not have move temporary the gav file to original location!")
                .isTrue();
    }

}
