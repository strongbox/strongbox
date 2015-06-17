package org.carlspring.strongbox.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.carlspring.strongbox.security.encryption.EncryptionAlgorithmsEnum;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author mtodorov
 */
public class MultipleDigestInputStreamTest
{


    @Test
    public void testRead()
            throws IOException,
                   NoSuchAlgorithmException
    {
        String s = "This is a big fat super long text which has no meaning, but is good for the test.";

        ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());
        MultipleDigestInputStream mdis = new MultipleDigestInputStream(bais);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int size = 16;
        byte[] bytes = new byte[size];
        int len;

        //noinspection StatementWithEmptyBody
        while ((len = mdis.read(bytes, 0, size)) != -1)
        {
            baos.write(bytes, 0, len);
        }

        baos.flush();

        System.out.println(new String(baos.toByteArray()));

        final String md5 = mdis.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        final String sha1 = mdis.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        assertEquals("Incorrect MD5 sum!", "693188a2fb009bf2a87afcbca95cfcd6", md5);
        assertEquals("Incorrect SHA-1 sum!", "6ed7c74babd1609cb11836279672ade14a8748c1", sha1);

        System.out.println("MD5:  " + md5);
        System.out.println("SHA1: " + sha1);
    }

}
