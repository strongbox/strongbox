package org.carlspring.strongbox.io;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.CommonConfig;
import org.carlspring.strongbox.StorageApiConfig;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ArtifactFileOutputStreamTest
        extends TestCaseWithArtifactGeneration
{

    @org.springframework.context.annotation.Configuration
    @Import({
            StorageApiConfig.class,
            CommonConfig.class
    })
    public static class SpringConfig { }

    @Autowired
    private ConfigurationManager configurationManager;

    @Test
    public void testCreateWithTemporaryLocation()
            throws IOException
    {
        final Storage storage = getConfiguration().getStorage("storage0");
        final Repository repository = storage.getRepository("releases");

        final Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.foo:temp-file-test:1.2.3:jar");
        final ArtifactFile artifactFile = new ArtifactFile(repository, artifact, true);
        artifactFile.createParents();

        final ArtifactFileOutputStream afos = new ArtifactFileOutputStream(artifactFile);

        ByteArrayInputStream bais = new ByteArrayInputStream("This is a test\n".getBytes());
        IOUtils.copy(bais, afos);

        assertTrue("Failed to create temporary artifact file!", artifactFile.getTemporaryFile().exists());

        afos.close();

        assertTrue("Failed to the move temporary artifact file to original location!", afos.getArtifactFile().exists());
    }

    @Test
    public void testCreateWithTemporaryLocationNoMoveOnClose()
            throws IOException
    {
        final Storage storage = getConfiguration().getStorage("storage0");
        final Repository repository = storage.getRepository("releases");

        final Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.foo:temp-file-test:1.2.4:jar");
        final ArtifactFile artifactFile = new ArtifactFile(repository, artifact, true);
        artifactFile.createParents();

        final ArtifactFileOutputStream afos = new ArtifactFileOutputStream(artifactFile, false);

        ByteArrayInputStream bais = new ByteArrayInputStream("This is a test\n".getBytes());
        IOUtils.copy(bais, afos);

        assertTrue("Failed to create temporary artifact file!", artifactFile.getTemporaryFile().exists());

        afos.close();

        assertFalse("Should not have move temporary the artifact file to original location!",
                    afos.getArtifactFile().exists());
        assertTrue("Should not have move temporary the artifact file to original location!",
                   afos.getArtifactFile().getTemporaryFile().exists());
    }

    private Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
