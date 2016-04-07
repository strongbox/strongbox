package org.carlspring.strongbox.io;

import org.carlspring.commons.http.range.ByteRange;
import org.carlspring.commons.io.reloading.FSReloadableInputStreamHandler;
import org.carlspring.commons.io.reloading.ReloadableInputStreamHandler;
import org.carlspring.strongbox.security.encryption.EncryptionAlgorithmsEnum;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author mtodorov
 */
public class MultipleDigestInputStreamTest
{


    @Before
    public void setUp() throws Exception
    {
        File testResourcesDir = new File("target/test-resources");
        if (!testResourcesDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            testResourcesDir.mkdirs();
        }
    }

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

    @Test
    public void testReloading()
            throws IOException, NoSuchAlgorithmException
    {
        File f = new File("target/test-resources/test-stream-reloading.txt");

        FileOutputStream fos = new FileOutputStream(f);
        fos.write("This is a test.\n".getBytes());
        fos.flush();
        fos.close();

        ByteRange byteRange1 = new ByteRange(0, 10);
        ByteRange byteRange2 = new ByteRange(11, 21);

        List<ByteRange> byteRanges = new ArrayList<>();
        byteRanges.add(byteRange1);
        byteRanges.add(byteRange2);

        ReloadableInputStreamHandler handler = new FSReloadableInputStreamHandler(f);
        ArtifactInputStream ais = new ArtifactInputStream(handler, byteRanges);
        ais.setLimit(1);

        long len = 0L;
        while (ais.read() != -1)
        {
            len++;
        }

        assertEquals("Failed to limit byte range!", 1L, len);

        ais.reload();
        ais.setLimit(3);

        len = 0;

        while (ais.read() != -1)
        {
            len++;
        }

        assertEquals("Failed to limit byte range!", 2L, len);
    }

}
