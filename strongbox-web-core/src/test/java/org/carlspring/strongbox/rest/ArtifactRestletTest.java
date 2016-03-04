package org.carlspring.strongbox.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactDeployer;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.security.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author mtodorov
 */
public class ArtifactRestletTest
        extends TestCaseWithArtifactGeneration
{

    private static final File REPOSITORY_BASEDIR_RELEASES = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/storage0/releases");

    public static boolean INITIALIZED = false;

    private RestClient client = new RestClient();


    @Before
    public void setUp()
            throws Exception
    {
        if (!INITIALIZED)
        {
            // Generate releases
            // Used by testPartialFetch():
                generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                                 "org.carlspring.strongbox.partial:partial-foo",
                                 new String[]{ "3.1", // Used by testPartialFetch()
                                               "3.2"  // Used by testPartialFetch()
                                             });
    
                // Used by testCopy*():
                generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                                 "org.carlspring.strongbox.copy:copy-foo",
                                 new String[]{ "1.1", // Used by testCopyArtifactFile()
                                               "1.2"  // Used by testCopyArtifactDirectory()
                                             });
    
                // Used by testDelete():
                generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                                 "com.artifacts.to.delete.releases:delete-foo",
                                 new String[]{ "1.2.1", // Used by testDeleteArtifactFile
                                               "1.2.2"  // Used by testDeleteArtifactDirectory
                                             });
                
                generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                        "org.carlspring.strongbox.partial:partial-foo",
                        new String[]{ "3.1", // Used by testPartialFetch()
                                      "3.2"  // Used by testPartialFetch()
                                    });

            INITIALIZED = true;
        }
    }

    @After
    public void tearDown()
            throws Exception
    {
        if (client != null)
        {
            client.close();
        }
    }

    @Test
    public void testPartialFetch()
            throws Exception
    {
        String artifactPath = "/storages/storage0/releases/org/carlspring/strongbox/partial/partial-foo/3.1/partial-foo-3.1.jar";

        assertTrue("Artifact does not exist!", client.pathExists(artifactPath));

        String md5Remote = MessageDigestUtils.readChecksumFile(client.getResource(artifactPath + ".md5"));
        String sha1Remote = MessageDigestUtils.readChecksumFile(client.getResource(artifactPath + ".sha1"));

        InputStream is = client.getResource(artifactPath);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(baos);

        int size = 1024;
        byte[] bytes = new byte[size];
        int total = 0;
        int len;

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            mdos.write(bytes, 0, len);

            total += len;
            if (total >= size)
            {
                break;
            }
        }

        mdos.flush();

        bytes = new byte[size];
        is.close();

        System.out.println("Read " + total + " bytes.");

        is = client.getResource(artifactPath, total);

        System.out.println("Skipped " + total + " bytes.");

        int partialRead = total;
        int len2 = 0;

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            mdos.write(bytes, 0, len);

            len2 += len;
            total += len;
        }

        mdos.flush();

        System.out.println("Wrote " + total + " bytes.");
        System.out.println("Partial read, terminated after writing " + partialRead + " bytes.");
        System.out.println("Partial read, continued and wrote " + len2 + " bytes.");
        System.out.println("Partial reads: total written bytes: " + (partialRead + len2) + ".");

        mdos.close();

        final String md5Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        final String sha1Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        System.out.println("MD5   [Remote]: " + md5Remote);
        System.out.println("MD5   [Local ]: " + md5Local);

        System.out.println("SHA-1 [Remote]: " + sha1Remote);
        System.out.println("SHA-1 [Local ]: " + sha1Local);

        FileOutputStream output = new FileOutputStream(new File("target/partial-foo-3.1.jar"));
        output.write(baos.toByteArray());

        assertEquals("Glued partial fetches did not match MD5 checksum!", md5Remote, md5Local);
        assertEquals("Glued partial fetches did not match SHA-1 checksum!", sha1Remote, sha1Local);
    }

    @Test
    public void testCopyArtifactFile()
            throws Exception
    {
        final File destRepositoryBasedir = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                    "/storages/storage0/releases-with-trash");

        String artifactPath = "org/carlspring/strongbox/copy/copy-foo/1.1/copy-foo-1.1.jar";

        File artifactFileRestoredFromTrash = new File(destRepositoryBasedir + "/" + artifactPath).getAbsoluteFile();

        assertFalse("Unexpected artifact in repository '" + destRepositoryBasedir + "'!",
                    artifactFileRestoredFromTrash.exists());

        client.copy(artifactPath,
                    "storage0",
                    "releases",
                    "storage0",
                    "releases-with-trash");

        assertTrue("Failed to copy artifact to destination repository '" + destRepositoryBasedir + "'!",
                   artifactFileRestoredFromTrash.exists());
    }

    @Test
    public void testCopyArtifactDirectory()
            throws Exception
    {
        final File destRepositoryBasedir = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                    "/storages/storage0/releases-with-trash");

        String artifactPath = "org/carlspring/strongbox/copy/copy-foo/1.2";

        File artifactFileRestoredFromTrash = new File(destRepositoryBasedir + "/" + artifactPath).getAbsoluteFile();

        assertFalse("Unexpected artifact in repository '" + destRepositoryBasedir + "'!",
                    artifactFileRestoredFromTrash.exists());

        client.copy(artifactPath,
                    "storage0",
                    "releases",
                    "storage0",
                    "releases-with-trash");

        assertTrue("Failed to copy artifact to destination repository '" + destRepositoryBasedir + "'!",
                   artifactFileRestoredFromTrash.exists());
    }

    @Test
    public void testDeleteArtifactFile()
            throws Exception
    {
        String artifactPath = "com/artifacts/to/delete/releases/delete-foo/1.2.1/delete-foo-1.2.1.jar";

        File deletedArtifact = new File(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath() + "/" + artifactPath).getAbsoluteFile();

        assertTrue("Failed to locate artifact file '" + deletedArtifact.getAbsolutePath() + "'!", deletedArtifact.exists());

        client.delete("storage0", "releases", artifactPath);

        assertFalse("Failed to delete artifact file '" + deletedArtifact.getAbsolutePath() + "'!", deletedArtifact.exists());
    }

    @Test
    public void testDeleteArtifactDirectory()
            throws Exception
    {
        String artifactPath = "com/artifacts/to/delete/releases/delete-foo/1.2.2";

        File deletedArtifact = new File(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath() + "/" + artifactPath).getAbsoluteFile();

        assertTrue("Failed to locate artifact file '" + deletedArtifact.getAbsolutePath() + "'!", deletedArtifact.exists());

        client.delete("storage0", "releases", artifactPath);

        assertFalse("Failed to delete artifact file '" + deletedArtifact.getAbsolutePath() + "'!", deletedArtifact.exists());
    }
    
    @Test
    @Ignore
    public void deployArtifactNewMetadataTest() throws NoSuchAlgorithmException, ArtifactOperationException, IOException, XmlPullParserException{
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.juan:juan-foo:3.3");
        Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.juan:juan-foo:3.4");
        Artifact artifact4 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.juan:juan-foo:3.5");
        Artifact artifact5 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.juan:juan-foo:3.6");

        ArtifactDeployer artifactDeployer = new ArtifactDeployer(REPOSITORY_BASEDIR_RELEASES);
        artifactDeployer.setClient(client);

        artifactDeployer.generateAndDeployArtifact(artifact2, "storage0", "releases");
        artifactDeployer.generateAndDeployArtifact(artifact3, "storage0", "releases");
        artifactDeployer.generateAndDeployArtifact(artifact4, "storage0", "releases");
        artifactDeployer.generateAndDeployArtifact(artifact5, "storage0", "releases");
        
        
    }

}