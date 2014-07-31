package org.carlspring.strongbox.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author mtodorov
 */
public class ArtifactInputStreamTest
{


    @Test
    public void testRead()
            throws IOException,
                   NoSuchAlgorithmException
    {
        String s = "This is a test.";

        ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());
        MultipleDigestInputStream mdis = new MultipleDigestInputStream(bais);
        
        byte[] bytes = new byte[64];

        //noinspection StatementWithEmptyBody
        while (mdis.read(bytes, 0, 64) != -1);

        final String md5 = mdis.getMessageDigestAsHexadecimalString("MD5");
        final String sha1 = mdis.getMessageDigestAsHexadecimalString("SHA-1");

        Assert.assertEquals("Incorrect MD5 sum!", "120ea8a25e5d487bf68b5f7096440019", md5);
        Assert.assertEquals("Incorrect SHA-1 sum!", "afa6c8b3a2fae95785dc7d9685a57835d703ac88", sha1);

        System.out.println("MD5:  " + md5);
        System.out.println("SHA1: " + sha1);
    }

}
