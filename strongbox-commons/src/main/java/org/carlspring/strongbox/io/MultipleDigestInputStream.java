package org.carlspring.strongbox.io;

import org.carlspring.strongbox.http.range.ByteRange;
import org.carlspring.strongbox.io.reloading.ReloadableInputStreamHandler;
import org.carlspring.strongbox.io.reloading.Reloading;
import org.carlspring.strongbox.io.reloading.Repositioning;
import org.carlspring.strongbox.security.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.util.MessageDigestUtils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * This class is based on java.security.DigestInputStream.
 *
 * @author mtodorov
 */
public class MultipleDigestInputStream
        extends FilterInputStream
        implements Reloading,
                   Repositioning
{

    public static final String[] DEFAULT_ALGORITHMS = { EncryptionAlgorithmsEnum.MD5.getAlgorithm(),
                                                        EncryptionAlgorithmsEnum.SHA1.getAlgorithm() };

    private Map<String, MessageDigest> digests = new LinkedHashMap<>();

    private Map<String, String> hexDigests = new LinkedHashMap<>();

    /**
     * The number of bytes to read from the start of the stream, before stopping to read.
     */
    private long limit = 0L;

    /**
     * The number of bytes read from the stream, or from this byte range.
     */
    private long bytesRead = 0L;

    private List<ByteRange> byteRanges = new ArrayList<>();

    private ByteRange currentByteRange;

    private int currentByteRangeIndex = 0;

    private ReloadableInputStreamHandler reloadableInputStreamHandler;


    public MultipleDigestInputStream(ReloadableInputStreamHandler handler, ByteRange byteRange)
            throws IOException, NoSuchAlgorithmException
    {
        super(handler.getInputStream());

        List<ByteRange> byteRanges = new ArrayList<>();
        byteRanges.add(byteRange);

        this.reloadableInputStreamHandler = handler;
        this.byteRanges = byteRanges;
        this.currentByteRange = byteRanges.get(0);
    }

    public MultipleDigestInputStream(ReloadableInputStreamHandler handler, List<ByteRange> byteRanges)
            throws IOException, NoSuchAlgorithmException
    {
        super(handler.getInputStream());
        this.reloadableInputStreamHandler = handler;
        this.byteRanges = byteRanges;
        this.currentByteRange = byteRanges.get(0);
    }

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
        if (hasReachedLimit())
        {
            return -1;
        }

        int ch = in.read();
        if (ch != -1)
        {
            for (Map.Entry entry : digests.entrySet())
            {
                MessageDigest digest = (MessageDigest) entry.getValue();
                digest.update((byte) ch);
            }
        }

        bytesRead++;

        return ch;
    }

    @Override
    public int read(byte[] bytes,
                    int off,
                    int len)
            throws IOException
    {
        if (hasReachedLimit())
        {
            return -1;
        }

        int numberOfBytesRead = in.read(bytes, off, len);
        if (numberOfBytesRead != -1)
        {
            for (Map.Entry entry : digests.entrySet())
            {
                MessageDigest digest = (MessageDigest) entry.getValue();
                digest.update(bytes, off, numberOfBytesRead);
            }
        }

        if (limit > 0 && bytesRead < limit)
        {
            bytesRead += numberOfBytesRead;
        }

        return numberOfBytesRead;
    }

    @Override
    public int read(byte[] bytes)
            throws IOException
    {
        if (hasReachedLimit())
        {
            return -1;
        }

        int len = in.read(bytes);

        for (Map.Entry entry : digests.entrySet())
        {
            MessageDigest digest = (MessageDigest) entry.getValue();
            digest.update(bytes);
        }

        bytesRead += len;

        if (limit > 0 && bytesRead < limit)
        {
            bytesRead += len;
        }

        return len;
    }

    @Override
    public void reload()
            throws IOException
    {
        reloadableInputStreamHandler.reload();
        in = reloadableInputStreamHandler.getInputStream();
    }

    private boolean hasReachedLimit()
    {
        return limit > 0 && bytesRead >= limit;
    }

    public long getLimit()
    {
        return limit;
    }

    public void setLimit(long limit)
    {
        this.limit = limit;
    }

    public long getBytesRead()
    {
        return bytesRead;
    }

    public void setBytesRead(long bytesRead)
    {
        this.bytesRead = bytesRead;
    }

    public ReloadableInputStreamHandler getReloadableInputStreamHandler()
    {
        return this.reloadableInputStreamHandler;
    }

    public void setReloadableInputStreamHandler(ReloadableInputStreamHandler reloadableInputStreamHandler)
    {
        this.reloadableInputStreamHandler = reloadableInputStreamHandler;
    }

    public List<ByteRange> getByteRanges()
    {
        return byteRanges;
    }

    public void setByteRanges(List<ByteRange> byteRanges)
    {
        this.byteRanges = byteRanges;
    }

    public ByteRange getCurrentByteRange()
    {
        return currentByteRange;
    }

    public void setCurrentByteRange(ByteRange currentByteRange)
    {
        this.currentByteRange = currentByteRange;
    }

    @Override
    public void reposition()
            throws IOException
    {
        if (byteRanges != null && !byteRanges.isEmpty() && currentByteRangeIndex < byteRanges.size())
        {
            if (currentByteRangeIndex < byteRanges.size())
            {
                ByteRange current = currentByteRange;

                currentByteRangeIndex++;
                currentByteRange = byteRanges.get(currentByteRangeIndex);

                if (currentByteRange.getOffset() > current.getLimit())
                {
                    // If the offset is higher than the current position, skip forward
                    long bytesToSkip = currentByteRange.getOffset() - current.getLimit();

                    //noinspection ResultOfMethodCallIgnored
                    in.skip(bytesToSkip);
                }
                else
                {
                    reloadableInputStreamHandler.reload();
                    in = reloadableInputStreamHandler.getInputStream();
                }
            }
        }
    }

    @Override
    public void reposition(long skipBytes)
            throws IOException
    {

    }

}
