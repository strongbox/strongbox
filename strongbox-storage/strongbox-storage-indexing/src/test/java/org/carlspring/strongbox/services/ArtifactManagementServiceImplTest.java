package org.carlspring.strongbox.services;

import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;

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
            generator.generate(gavtc, "7.2");

            generator.setBasedir(STORAGE_BASEDIR.getAbsolutePath() + "/releases-with-redeployment");
            generator.generate(gavtc, "7.3");

            INITIALIZED = true;
        }
    }

    @Test
    public void testDeploymentToRepositoryWithForbiddenDeployments()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        InputStream is = null;

        //noinspection EmptyCatchBlock
        try
        {
            String repositoryId = "releases-without-deployment";
            String gavtc = "org.carlspring.strongbox:strongbox-utils:8.0:jar";

            is = generateArtifactInputStream(repositoryId, gavtc, true);

            Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            artifactManagementService.store("storage0",
                                            repositoryId,
                                            ArtifactUtils.convertArtifactToPath(artifact),
                                            is);

            fail("Failed to deny artifact operation for repository with disallowed deployments.");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
        finally
        {
            ResourceCloser.close(is, null);
        }
    }

    @Test
    public void testRedeploymentToRepositoryWithForbiddenRedeployments()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        InputStream is = null;

        //noinspection EmptyCatchBlock
        try
        {
            String repositoryId = "releases-without-redeployment";
            String gavtc = "org.carlspring.strongbox:strongbox-utils:8.1:jar";

            generateArtifact(gavtc, new File(STORAGE_BASEDIR, repositoryId).getAbsolutePath());

            is = generateArtifactInputStream(repositoryId, gavtc, true);

            Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            artifactManagementService.store("storage0",
                                            repositoryId,
                                            ArtifactUtils.convertArtifactToPath(artifact),
                                            is);

            fail("Failed to deny artifact operation for repository with disallowed re-deployments.");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
        finally
        {
            ResourceCloser.close(is, null);
        }
    }

    @Test
    public void testDeletionFromRepositoryWithForbiddenDeletes()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        InputStream is = null;

        //noinspection EmptyCatchBlock
        try
        {
            String repositoryId = "releases-without-delete";
            String gavtc = "org.carlspring.strongbox:strongbox-utils:8.2:jar";

            generateArtifact(gavtc, new File(STORAGE_BASEDIR, repositoryId).getAbsolutePath());

            is = generateArtifactInputStream(repositoryId, gavtc, true);

            Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            artifactManagementService.delete("storage0",
                                             repositoryId,
                                             ArtifactUtils.convertArtifactToPath(artifact),
                                             false);

            fail("Failed to deny artifact operation for repository with disallowed deletions.");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
        finally
        {
            ResourceCloser.close(is, null);
        }
    }

    @Test
    public void testDeploymentRedeploymentAndDeletionAgainstGroupRepository()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        InputStream is = null;

        String repositoryId = "group-releases";
        String gavtc = "org.carlspring.strongbox:strongbox-utils:8.3:jar";

        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);

        //noinspection EmptyCatchBlock
        try
        {
            is = generateArtifactInputStream(repositoryId, gavtc, true);

            artifactManagementService.store("storage0",
                                            repositoryId,
                                            ArtifactUtils.convertArtifactToPath(artifact),
                                            is);

            fail("Failed to deny artifact operation for repository with disallowed deployments.");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
        finally
        {
            ResourceCloser.close(is, null);
        }

        //noinspection EmptyCatchBlock
        try
        {
            // Generate the artifact on the file-system anyway so that we could achieve
            // the state of having it there before attempting a re-deployment
            generateArtifact(gavtc, new File(STORAGE_BASEDIR, repositoryId).getAbsolutePath());
            artifactManagementService.store("storage0",
                                            repositoryId,
                                            ArtifactUtils.convertArtifactToPath(artifact),
                                            is);

            fail("Failed to deny artifact operation for repository with disallowed re-deployments.");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
        finally
        {
            ResourceCloser.close(is, null);
        }

        // Delete: Case 1: No forcing
        //noinspection EmptyCatchBlock
        try
        {
            artifactManagementService.delete("storage0",
                                             repositoryId,
                                             ArtifactUtils.convertArtifactToPath(artifact),
                                             false);

            fail("Failed to deny artifact operation for repository with disallowed deletions (non-forced test).");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
        finally
        {
            ResourceCloser.close(is, null);
        }

        // Delete: Case 2: Force delete
        //noinspection EmptyCatchBlock
        try
        {
            artifactManagementService.delete("storage0",
                                             repositoryId,
                                             ArtifactUtils.convertArtifactToPath(artifact),
                                             true);

            fail("Failed to deny artifact operation for repository with disallowed deletions (forced test).");
        }
        catch (ArtifactStorageException e)
        {
            // This is the expected correct behavior
        }
        finally
        {
            ResourceCloser.close(is, null);
        }
    }

    @Test
    public void testArtifactResolutionFromGroup()
            throws IOException
    {
        InputStream is = artifactManagementService.resolve("storage0",
                                                           "group-releases",
                                                           "org/carlspring/strongbox/strongbox-utils/7.3/strongbox-utils-7.3.jar");


        assertFalse("Failed to resolve artifact from group repository!", is == null);
        assertTrue("Failed to resolve artifact from group repository!", is.available() > 0);

        is.close();
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

        assertFalse("Failed to delete artifact during a force delete operation!",
                    new File(REPOSITORY_BASEDIR, artifactPath1).exists());

        final String artifactPath2 = "org/carlspring/strongbox/strongbox-utils/7.2/strongbox-utils-7.2.jar";
        artifactManagementService.delete("storage0",
                                         "releases-with-trash",
                                         artifactPath2,
                                         true);

        final File repositoryDir = new File(STORAGE_BASEDIR, "releases-with-trash/.trash");

        assertTrue("Should have moved the artifact to the trash during a force delete operation, " +
                   "when allowsForceDeletion is not enabled!",
                   new File(repositoryDir, artifactPath2).exists());
    }

    private InputStream generateArtifactInputStream(String repositoryId, String gavtc, boolean useTempDir)
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        File basedir = new File(STORAGE_BASEDIR.getAbsolutePath() + "/" + repositoryId + (useTempDir ? "/.temp" : ""));
        if (!basedir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            basedir.mkdirs();
        }

        Artifact artifact = generateArtifact(gavtc, basedir.getCanonicalPath());

        return new FileInputStream(new File(basedir, ArtifactUtils.convertArtifactToPath(artifact)));
    }

    private Artifact generateArtifact(String gavtc, String basedir)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);

        ArtifactGenerator generator = new ArtifactGenerator(basedir);
        generator.generate(artifact);

        return artifact;
    }

}
