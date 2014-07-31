package org.carlspring.strongbox.io;

import org.carlspring.strongbox.security.encryption.EncryptionConstants;
import org.carlspring.strongbox.util.MessageDigestUtils;

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

    public static final String[] DEFAULT_ALGORITHMS = { EncryptionConstants.ALGORITHM_MD5,
                                                        EncryptionConstants.ALGORITHM_SHA1 };


    public MultipleDigestInputStream(InputStream is)
            throws NoSuchAlgorithmException
    {
        this(is, DEFAULT_ALGORITHMS);
    }

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

    public Map<String, MessageDigest> getDigests()
    {
        return digests;
    }

    public String getMessageDigestAsHexadecimalString(String algorithm)
    {
        return MessageDigestUtils.convertToHexadecimalString(getMessageDigest(algorithm));
    }

    public void setDigests(Map<String, MessageDigest> digests)
    {
        this.digests = digests;
    }

    @Override
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

    @Override
    public int read(byte[] bytes,
                    int off,
                    int len)
            throws IOException
    {
        int numberOfBytesRead = in.read(bytes, off, len);
        if (numberOfBytesRead != -1)
        {
            for (Map.Entry entry : digests.entrySet())
            {
                MessageDigest digest = (MessageDigest) entry.getValue();
                digest.update(bytes, off, numberOfBytesRead);
            }
        }

        return numberOfBytesRead;
    }

}
