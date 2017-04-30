package org.carlspring.strongbox.io;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.io.ArtifactPath;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemProvider;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ArtifactOutputStreamTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    public static final String REPOSITORY_RELEASES = "aos-releases";


    @PostConstruct
    public void initialize()
            throws Exception
    {
        createRepository(STORAGE0, REPOSITORY_RELEASES, false);
    }

    @PreDestroy
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES));

        return repositories;
    }

    @Test
    public void testCreateWithTemporaryLocation()
            throws IOException,
                   NoSuchAlgorithmException
    {
        final Storage storage = getConfiguration().getStorage("storage0");
        final Repository repository = storage.getRepository("releases");

        final Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.foo:temp-file-test:1.2.3:jar");
        final ArtifactCoordinates coordinates = new MavenArtifactCoordinates(artifact);
        ArtifactPath artifactPath = ArtifactPath.getArtifactPath(repository, coordinates);
        RepositoryFileSystemProvider provider = (RepositoryFileSystemProvider) artifactPath.getFileSystem()
                                                                                           .provider();
        RepositoryPath artifactPathTemp = provider.getTempPath(artifactPath);

        final ArtifactOutputStream afos = new ArtifactOutputStream(Files.newOutputStream(artifactPathTemp),
                                                                   coordinates);
        ByteArrayInputStream bais = new ByteArrayInputStream("This is a test\n".getBytes());
        IOUtils.copy(bais, afos);

        assertTrue("Failed to create temporary artifact file!", Files.exists(artifactPathTemp));

        afos.close();
        provider.moveFromTemporaryDirectory(artifactPath);

        assertTrue("Failed to the move temporary artifact file to original location!", Files.exists(artifactPath));
    }

    @Test
    public void testCreateWithTemporaryLocationNoMoveOnClose()
            throws IOException,
                   NoSuchAlgorithmException
    {
        final Storage storage = getConfiguration().getStorage("storage0");
        final Repository repository = storage.getRepository("releases");

        final Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.foo:temp-file-test:1.2.4:jar");
        final ArtifactCoordinates coordinates = new MavenArtifactCoordinates(artifact);

        ArtifactPath artifactPath = ArtifactPath.getArtifactPath(repository, coordinates);

        RepositoryFileSystemProvider provider = (RepositoryFileSystemProvider) artifactPath.getFileSystem()
                                                                                           .provider();
        RepositoryPath artifactPathTemp = provider.getTempPath(artifactPath);

        final ArtifactOutputStream afos = new ArtifactOutputStream(Files.newOutputStream(artifactPathTemp),
                                                                   coordinates);
        ByteArrayInputStream bais = new ByteArrayInputStream("This is a test\n".getBytes());
        IOUtils.copy(bais, afos);

        assertTrue("Failed to create temporary artifact file!", Files.exists(artifactPathTemp));

        afos.close();

        assertFalse("Should not have move temporary the artifact file to original location!",
                    Files.exists(artifactPath));
        assertTrue("Should not have move temporary the artifact file to original location!",
                   Files.exists(artifactPathTemp));
    }

}
