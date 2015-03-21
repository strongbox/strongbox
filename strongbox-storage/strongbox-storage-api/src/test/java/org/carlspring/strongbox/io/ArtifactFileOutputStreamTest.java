package org.carlspring.strongbox.io;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class ArtifactFileOutputStreamTest
        extends TestCaseWithArtifactGeneration
{

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

        afos.close();

        assertTrue(afos.getArtifactFile().exists());
    }

    private Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
