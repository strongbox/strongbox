package org.carlspring.strongbox.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.storage.FileSystemStorageProvider;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ArtifactOutputStreamTest
        extends TestCaseWithArtifactGeneration
{

    @org.springframework.context.annotation.Configuration
    @Import({ StorageApiConfig.class,
              CommonConfig.class
              })
    
    public static class SpringConfig { }

    @Autowired
    private ConfigurationManager configurationManager;

    @Test
    public void testCreateWithTemporaryLocation()
            throws IOException, NoSuchAlgorithmException
    {
        final Storage storage = getConfiguration().getStorage("storage0");
        final Repository repository = storage.getRepository("releases");

        final Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.foo:temp-file-test:1.2.3:jar");
        final ArtifactCoordinates coordinates = new MavenArtifactCoordinates(artifact);
        ArtifactPath artifactPath = new ArtifactPath(coordinates,
                                                     FileSystemStorageProvider.getArtifactPath(repository.getBasedir(), coordinates.toPath()),
                                                     FileSystemStorageProvider.getRepositoryFileSystem(repository));
        RepositoryFileSystemProvider provider = (RepositoryFileSystemProvider) artifactPath.getFileSystem().provider();
        RepositoryPath artifactPathTemp = provider.getTempPath(artifactPath);

        final ArtifactOutputStream afos = new ArtifactOutputStream(Files.newOutputStream(artifactPathTemp), coordinates);
        ByteArrayInputStream bais = new ByteArrayInputStream("This is a test\n".getBytes());
        IOUtils.copy(bais, afos);
        
        assertTrue("Failed to create temporary artifact file!", Files.exists(artifactPathTemp));
        
        afos.close();
        provider.restoreFromTemp(artifactPath);
        
        assertTrue("Failed to the move temporary artifact file to original location!", Files.exists(artifactPath));
    }

    @Test
    public void testCreateWithTemporaryLocationNoMoveOnClose()
            throws IOException, NoSuchAlgorithmException
    {
        final Storage storage = getConfiguration().getStorage("storage0");
        final Repository repository = storage.getRepository("releases");

        final Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.foo:temp-file-test:1.2.4:jar");
        final ArtifactCoordinates coordinates = new MavenArtifactCoordinates(artifact);
        
        ArtifactPath artifactPath = new ArtifactPath(coordinates,
                                                     FileSystemStorageProvider.getArtifactPath(repository.getBasedir(), coordinates.toPath()),
                                                     FileSystemStorageProvider.getRepositoryFileSystem(repository));
                                                     
        RepositoryFileSystemProvider provider = (RepositoryFileSystemProvider) artifactPath.getFileSystem().provider();
        RepositoryPath artifactPathTemp = provider.getTempPath(artifactPath);

        final ArtifactOutputStream afos = new ArtifactOutputStream(Files.newOutputStream(artifactPathTemp), coordinates);
        ByteArrayInputStream bais = new ByteArrayInputStream("This is a test\n".getBytes());
        IOUtils.copy(bais, afos);
        
        assertTrue("Failed to create temporary artifact file!", Files.exists(artifactPathTemp));
        
        afos.close();
        
        assertFalse("Should not have move temporary the artifact file to original location!", Files.exists(artifactPath));
        assertTrue("Should not have move temporary the artifact file to original location!", Files.exists(artifactPathTemp));
    }

    private Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
