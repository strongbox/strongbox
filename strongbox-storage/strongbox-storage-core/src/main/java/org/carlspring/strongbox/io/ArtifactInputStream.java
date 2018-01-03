package org.carlspring.strongbox.io;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.util.MessageDigestUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

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
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note that this class is "abstract" and you don't need to instantiate it directly, see example below:
 * <pre>
 * ...
 * RepositoryPath repositoryPath = layoutProvider.resolve("path/to/your/artifact/file.ext");
 * ArtifactInputStream aos = (ArtifactInputStream) Files.newInputStream(repositoryPath);
 * ...
 * </pre>
 *
 * @author mtodorov
 */
public abstract class ArtifactInputStream
        extends FilterInputStream
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactInputStream.class);
    public static final String[] DEFAULT_ALGORITHMS = { EncryptionAlgorithmsEnum.MD5.getAlgorithm(),
                                                        EncryptionAlgorithmsEnum.SHA1.getAlgorithm(),
                                                        };

    private ArtifactCoordinates artifactCoordinates;

    private Map<String, MessageDigest> digests = new LinkedHashMap<>();

    private Map<String, String> hexDigests = new LinkedHashMap<>();

    private String extension;

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

    private boolean isInputStreamValid()
            throws IOException
    {
        if (in.available() > 0)
        {
            return true;
        }
        else
        {
            return false;
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
            if (isInputStreamValid())
            {
                this.extension = getFileExtension(bytes);

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

    /**
     * This method reads array of input stream bytes to find out the file extension using Apache Tika.
     *
     * @param bytes
     * @return
     */
    public String getFileExtension(byte[] bytes)
    {
        String fileExtension = null;
        try
        {
            TikaConfig tika = new TikaConfig();

            MediaType mediaType = tika.getDetector().detect(TikaInputStream.get(bytes), new Metadata());
            MimeType mimeType = tika.getMimeRepository().forName(mediaType.toString());
            fileExtension = mimeType.getExtension();

        }
        catch (TikaException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return fileExtension;
    }

    public String getExtension()
    {
        return extension;
    }


}
