package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.security.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
public class ArtifactRestletTest
        extends TestCaseWithArtifactGeneration
{

    private static final File REPOSITORY_BASEDIR_RELEASES = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/releases");

    public static boolean INITIALIZED = false;

    private static RestClient client = new RestClient();


    @Before
    public void setUp()
            throws Exception
    {
        if (!INITIALIZED)
        {
            // Generate releases
            generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                             "org.carlspring.strongbox.partial:partial-foo",
                             new String[]{ "3.1", "3.2"});

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

}