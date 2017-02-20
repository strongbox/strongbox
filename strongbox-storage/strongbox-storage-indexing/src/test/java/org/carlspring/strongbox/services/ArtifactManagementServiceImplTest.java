package org.carlspring.strongbox.services;

import org.carlspring.maven.commons.io.filters.JarFilenameFilter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;
import org.carlspring.strongbox.util.FileUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ArtifactManagementServiceImplTest
        extends TestCaseWithArtifactGenerationWithIndexing
{

    private static final File STORAGE_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0");

    private static final File REPOSITORY_BASEDIR = new File(STORAGE_BASEDIR, "/releases");

    private static final File INDEX_DIR = new File(REPOSITORY_BASEDIR, ".index");

    private static final File REPOSITORY_BASEDIR_2 = new File(STORAGE_BASEDIR, "/snapshots");

    private DateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");

    @org.springframework.context.annotation.Configuration
    @Import({
                    CommonConfig.class,
                    StorageApiConfig.class
            })
    public static class SpringConfig { }

    @Inject
    private ArtifactManagementService artifactManagementService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

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

            generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                             gavtc,
                             new String[] { "7.0" // Used by testForceDelete()
                                          });
            generateArtifact(STORAGE_BASEDIR.getAbsolutePath() + "/releases-with-trash", gavtc, new String[] {"7.2"});
            generateArtifact(STORAGE_BASEDIR.getAbsolutePath() + "/releases-with-redeployment", gavtc, new String[] {"7.3"});

            createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_2.getAbsolutePath(),
                                              "org.carlspring.strongbox",
                                              "timestamped",
                                              "2.0",
                                              "jar",
                                              null,
                                              3);

            INITIALIZED = true;
        }
    }

    @Test
    public void testDeploymentToRepositoryWithForbiddenDeployments()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException,
                   ProviderImplementationException
    {
        InputStream is = null;

        //noinspection EmptyCatchBlock
        try
        {
            String repositoryId = "releases-without-deployment";
            String gavtc = "org.carlspring.strongbox:strongbox-utils:8.0:jar";

            File repositoryDir = new File(STORAGE_BASEDIR, repositoryId);
            is = generateArtifactInputStream(repositoryDir.getAbsolutePath(), repositoryId, gavtc, true);

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
                   IOException,
                   ProviderImplementationException
    {
        InputStream is = null;

        //noinspection EmptyCatchBlock
        try
        {
            String repositoryId = "releases-without-redeployment";
            String gavtc = "org.carlspring.strongbox:strongbox-utils:8.1:jar";

            generateArtifact(new File(STORAGE_BASEDIR, repositoryId).getAbsolutePath(), gavtc);

            File repositoryDir = new File(STORAGE_BASEDIR, repositoryId);
            is = generateArtifactInputStream(repositoryDir.getAbsolutePath(), repositoryId, gavtc, true);

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

            File repositoryDir = new File(STORAGE_BASEDIR, repositoryId);

            generateArtifact(repositoryDir.getAbsolutePath(), gavtc);

            is = generateArtifactInputStream(STORAGE_BASEDIR.getAbsolutePath(), repositoryId, gavtc, true);

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
                   IOException, ProviderImplementationException
    {
        InputStream is = null;

        String repositoryId = "group-releases";
        String gavtc = "org.carlspring.strongbox:strongbox-utils:8.3:jar";

        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);

        //noinspection EmptyCatchBlock
        try
        {
            File repositoryDir = new File(STORAGE_BASEDIR, repositoryId);
            is = generateArtifactInputStream(repositoryDir.getAbsolutePath(), repositoryId, gavtc, true);

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
            generateArtifact(new File(STORAGE_BASEDIR, repositoryId).getAbsolutePath(), gavtc);
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
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
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
            throws IOException
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

    @Test
    public void testRemoveTimestampedSnapshots()
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        String artifactPath = REPOSITORY_BASEDIR_2 + "/org/carlspring/strongbox/timestamped";

        File file = new File(artifactPath, "2.0-SNAPSHOT");

        assertEquals("Amount of timestamped snapshots doesn't equal 3.", 3,
                     file.listFiles(new JarFilenameFilter()).length);

        artifactMetadataService.rebuildMetadata("storage0", "snapshots", "org/carlspring/strongbox/timestamped");

        //To check removing timestamped snapshot with numberToKeep = 1
        artifactManagementService.removeTimestampedSnapshots("storage0", "snapshots",
                                                             "org/carlspring/strongbox/timestamped", 1, 0);

        File[] files = file.listFiles(new JarFilenameFilter());
        Artifact artifact = ArtifactUtils.convertPathToArtifact(files[0].getPath());
        String artifactName = artifact.getVersion();

        assertEquals("Amount of timestamped snapshots doesn't equal 1.", 1, files.length);
        assertTrue(artifactName.endsWith("-3"));

        //Creating timestamped snapshot with another timestamp

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -5);
        String timestamp = formatter.format(cal.getTime());

        createTimestampedSnapshot(REPOSITORY_BASEDIR_2.getAbsolutePath(),
                                  "org.carlspring.strongbox",
                                  "timestamped",
                                  "2.0",
                                  "jar",
                                  null,
                                  2,
                                  timestamp);

        artifactMetadataService.rebuildMetadata("storage0", "snapshots", "org/carlspring/strongbox/timestamped");

        assertEquals("Amount of timestamped snapshots doesn't equal 2.", 2,
                     file.listFiles(new JarFilenameFilter()).length);

        //To check removing timestamped snapshot with keepPeriod = 3 and numberToKeep = 0
        artifactManagementService.removeTimestampedSnapshots("storage0", "snapshots",
                                                             "org/carlspring/strongbox/timestamped", 0, 3);

        files = file.listFiles(new JarFilenameFilter());
        artifact = ArtifactUtils.convertPathToArtifact(files[0].getPath());
        artifactName = artifact.getVersion();

        assertEquals("Amount of timestamped snapshots doesn't equal 1.", 1, files.length);
        assertTrue(artifactName.endsWith("-3"));



    }

}
