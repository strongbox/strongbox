package org.carlspring.strongbox.io;

import org.carlspring.strongbox.resource.ResourceCloser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.security.NoSuchAlgorithmException;

/**
 * @author mtodorov
 */
public class MultipleDigestOutputStreamTest
{


    @Before
    public void setUp()
            throws Exception
    {
        File dir = new File("target/test-resources");
        if (!dir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
    }

    @Test
    public void testWrite()
            throws IOException,
                   NoSuchAlgorithmException
    {
        String s = "This is a test.";

        File file = new File("target/test-resources/metadata.xml");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(file, baos);
        
        mdos.write(s.getBytes());
        mdos.flush();

        final String md5 = mdos.getMessageDigestAsHexadecimalString("MD5");
        final String sha1 = mdos.getMessageDigestAsHexadecimalString("SHA-1");

        Assert.assertEquals("Incorrect MD5 sum!", "120ea8a25e5d487bf68b5f7096440019", md5);
        Assert.assertEquals("Incorrect SHA-1 sum!", "afa6c8b3a2fae95785dc7d9685a57835d703ac88", sha1);

        mdos.close();

        System.out.println("MD5:  " + md5);
        System.out.println("SHA1: " + sha1);

        File md5File = new File(file.getAbsolutePath() + ".md5");
        File sha1File = new File(file.getAbsolutePath() + ".sha1");

        Assert.assertTrue("Failed to create md5 checksum file!", md5File.exists());
        Assert.assertTrue("Failed to create sha1 checksum file!", sha1File.exists());

        String md5ReadIn = readChecksumFile(md5File.getAbsolutePath());
        String sha1ReadIn = readChecksumFile(sha1File.getAbsolutePath());

        Assert.assertEquals("MD5 checksum file contains incorrect checksum!", md5, md5ReadIn);
        Assert.assertEquals("SHA-1 checksum file contains incorrect checksum!", sha1, sha1ReadIn);
    }

    private String readChecksumFile(String path)
            throws IOException
    {
        InputStream is = null;
        BufferedReader br = null;

        try
        {
            is = new FileInputStream(path);
            br = new BufferedReader(new InputStreamReader(is));

            return br.readLine();
        }
        finally
        {
            ResourceCloser.close(br, null);
            ResourceCloser.close(is, null);
        }
    }

}
