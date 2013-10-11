package org.carlspring.strongbox.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is based on java.security.DigestInputStream.
 *
 * @author mtodorov
 */
public class MultipleDigestInputStream
        extends FilterInputStream
{

    private Map<String, MessageDigest> digests = new LinkedHashMap<String, MessageDigest>();


    public MultipleDigestInputStream(InputStream is,
                                     String[] algorithms)
            throws NoSuchAlgorithmException
    {
        super(is);

        for (String algorithm : algorithms)
        {
            addAlgorithm(algorithm);
        }
    }

    public void addAlgorithm(String algorithm)
            throws NoSuchAlgorithmException
    {
        MessageDigest digest = MessageDigest.getInstance(algorithm);

        digests.put(algorithm, digest);
    }

    public MessageDigest getMessageDigest(String algorithm)
    {
        return digests.get(algorithm);
    }

    public int read()
            throws IOException
    {
        int ch = in.read();
        if (ch != -1)
        {
            for (Map.Entry entry : digests.entrySet())
            {
                MessageDigest digest = (MessageDigest) entry.getValue();
                digest.update((byte) ch);
            }
        }

        return ch;
    }

    public int read(byte[] b,
                    int off,
                    int len)
            throws IOException
    {
        int result = in.read(b, off, len);
        if (result != -1)
        {
            for (Map.Entry entry : digests.entrySet())
            {
                MessageDigest digest = (MessageDigest) entry.getValue();
                digest.update(b, off, result);
            }
        }

        return result;
    }

}
