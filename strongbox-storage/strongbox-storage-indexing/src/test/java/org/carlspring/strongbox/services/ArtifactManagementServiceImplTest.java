package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/strongbox-*-context.xml",
                                    "classpath*:/META-INF/spring/strongbox-*-context.xml" })
public class ArtifactManagementServiceImplTest
{

    private static final File STORAGE_BASEDIR = new File("target/storages/storage0");

    private static final File REPOSITORY_BASEDIR = new File(STORAGE_BASEDIR, "/releases");

    private static final File INDEX_DIR = new File(REPOSITORY_BASEDIR, ".index");

    @Autowired
    private ArtifactManagementService artifactManagementService;

    private static boolean INITIALIZED = false;


    @Before
    public void init()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        if (!INITIALIZED)
        {
            //noinspection ResultOfMethodCallIgnored
            INDEX_DIR.mkdirs();

            String gavtc = "org.carlspring.strongbox:strongbox-utils::jar";

            ArtifactGenerator generator = new ArtifactGenerator(REPOSITORY_BASEDIR.getAbsolutePath());
            generator.generate(gavtc, "6.0.1", "6.1.1", "6.2.1", "6.2.2-SNAPSHOT", "7.0", "7.1");

            generator.setBasedir(STORAGE_BASEDIR.getAbsolutePath() + "/releases-with-trash");
            generator.generate(gavtc, "7.1");

            INITIALIZED = true;
        }
    }

    @Test
    public void testForceDelete()
            throws ArtifactStorageException
    {
        final String artifactPath1 = "org/carlspring/strongbox/strongbox-utils/7.0/strongbox-utils-7.0.jar";
        artifactManagementService.delete("storage0",
                                         "releases",
                                         artifactPath1,
                                         true);

        Assert.assertFalse("Failed to delete artifact during a force delete operation!",
                           new File(REPOSITORY_BASEDIR, artifactPath1).exists());

        final String artifactPath2 = "org/carlspring/strongbox/strongbox-utils/7.1/strongbox-utils-7.1.jar";
        artifactManagementService.delete("storage0",
                                         "releases-with-trash",
                                         artifactPath2,
                                         true);

        final File repositoryDir = new File(STORAGE_BASEDIR, "releases-with-trash/.trash");
        Assert.assertTrue("Should have moved the artifact to the trash during a force delete operation, " +
                          "when allowsForceDeletion is not enabled!",
                          new File(repositoryDir, artifactPath2).exists());
    }

}
