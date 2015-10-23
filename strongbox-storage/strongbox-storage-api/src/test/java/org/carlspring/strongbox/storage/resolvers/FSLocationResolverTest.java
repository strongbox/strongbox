package org.carlspring.strongbox.storage.resolvers;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class FSLocationResolverTest
        extends TestCaseWithArtifactGeneration
{

    private static final File STORAGE_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0");

    private static final File REPOSITORY_BASEDIR_RELEASES = new File(STORAGE_BASEDIR, "releases");

    @Autowired
    private FSLocationResolver fsLocationResolver;

    @Autowired
    private ConfigurationManager configurationManager;

    public static boolean INITIALIZED = false;


    @Before
    public void setUp()
            throws Exception
    {
        if (!INITIALIZED)
        {
            generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                             "com.artifacts.to.delete.releases:delete-foo",
                             new String[] { "1.2.1", // testDeleteArtifact()
                                            "1.2.2"  // testDeleteArtifactDirectory()
                                          });

            INITIALIZED = true;
        }
    }

    @Test
    public void testDeleteArtifact()
            throws IOException, NoSuchAlgorithmException
    {
        Repository repository = configurationManager.getConfiguration().getStorage("storage0").getRepository("releases");
        String path = "com/artifacts/to/delete/releases/delete-foo/1.2.1/delete-foo-1.2.1.jar";
        File artifactFile = new File(repository.getBasedir(), path);

        assertTrue("Failed to locate artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());

        fsLocationResolver.delete("storage0", "releases", path, false);

        assertFalse("Failed to delete artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());
    }

    @Test
    public void testDeleteArtifactDirectory()
            throws IOException, NoSuchAlgorithmException
    {
        Repository repository = configurationManager.getConfiguration().getStorage("storage0").getRepository("releases");
        String path = "com/artifacts/to/delete/releases/delete-foo/1.2.2";
        File artifactFile = new File(repository.getBasedir(), path);

        assertTrue("Failed to locate artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());

        fsLocationResolver.delete("storage0", "releases", path, false);

        assertFalse("Failed to delete artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());
    }

}
