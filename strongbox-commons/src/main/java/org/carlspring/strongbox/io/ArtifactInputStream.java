package org.carlspring.strongbox.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.util.MessageDigestUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

/**
 * @author mtodorov
 */
public abstract class ArtifactInputStream
        extends FilterInputStream
{

    public static final String[] DEFAULT_ALGORITHMS = { EncryptionAlgorithmsEnum.MD5.getAlgorithm(),
                                                        EncryptionAlgorithmsEnum.SHA1.getAlgorithm(),
                                                        };

    private ArtifactCoordinates artifactCoordinates;

    private Map<String, MessageDigest> digests = new LinkedHashMap<>();

    private Map<String, String> hexDigests = new LinkedHashMap<>();

    public ArtifactInputStream(ArtifactCoordinates coordinates,
                               InputStream is,
                               Set<String> checkSumDigestAlgorithmSet)
        throws NoSuchAlgorithmException
    {
        super(is);
        this.artifactCoordinates = coordinates;
        for (String algorithm : checkSumDigestAlgorithmSet)
        {
            addAlgorithm(algorithm);
        }
    }

    public ArtifactInputStream(ArtifactCoordinates coordinates,
                               InputStream is)
        throws NoSuchAlgorithmException
    {
        this(coordinates, is, new HashSet<String>()
        {
            {
                add(MessageDigestAlgorithms.MD5);
                add(MessageDigestAlgorithms.SHA_1);
            }
        });
    }

    public ArtifactCoordinates getArtifactCoordinates()
    {
        return artifactCoordinates;
    }

    public final void addAlgorithm(String algorithm)
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

    public void resetHexDidests()
    {
        hexDigests.clear();
    }
    
    public Map<String, String> getHexDigests()
    {
        return hexDigests;
    }

    public String getMessageDigestAsHexadecimalString(String algorithm)
    {
        if (hexDigests.containsKey(algorithm))
        {
            return hexDigests.get(algorithm);
        }
        else
        {
            // This method will invoke MessageDigest.digest() which will reset the bytes when it's done
            // and thus this data will no longer be available, so we'll need to cache the calculated digest
            String hexDigest = MessageDigestUtils.convertToHexadecimalString(getMessageDigest(algorithm));
            hexDigests.put(algorithm, hexDigest);

            return hexDigest;
        }
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

    @Override
    public int read(byte[] bytes)
            throws IOException
    {
        int len = in.read(bytes);

        for (Map.Entry entry : digests.entrySet())
        {
            MessageDigest digest = (MessageDigest) entry.getValue();
            digest.update(bytes);
        }

        return len;
    }

    InputStream getTarget()
    {
        return in;
    }
    
}
