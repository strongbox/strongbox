package org.carlspring.strongbox.io;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.util.MessageDigestUtils;

/**
 * This OutputStream wraps a source stream from different Storage types (File System, AWS, JDBC, etc.).
 * 
 * @author Sergey Bespalov
 *
 */
public class ArtifactOutputStream
        extends MultipleDigestOutputStream
{
    private Function<byte[], String> digestStringifier = MessageDigestUtils::convertToHexadecimalString;
    private ArtifactCoordinates coordinates;
    /**
     * Used to cache Artifact contents if needed.
     */
    private OutputStream cacheOutputStream;
    private Function<OutputStreamFunction, ?> cacheOutputStreamTemplate = this::doWithOutputStream;

    public ArtifactOutputStream(OutputStream source,
                                ArtifactCoordinates coordinates)
        throws NoSuchAlgorithmException
    {
        super(source, new String[] {});
        this.coordinates = coordinates;
    }

    public void setCacheOutputStreamTemplate(Function<OutputStreamFunction, ?> chahceOutputStreamTemplate)
    {
        this.cacheOutputStreamTemplate = chahceOutputStreamTemplate;
    }

    public ArtifactCoordinates getCoordinates()
    {
        return coordinates;
    }

    public OutputStream getCacheOutputStream()
    {
        return cacheOutputStream;
    }

    public void setCacheOutputStream(OutputStream cacheOutputStream)
    {
        this.cacheOutputStream = cacheOutputStream;
    }

    public Function<byte[], String> getDigestStringifier()
    {
        return digestStringifier;
    }

    public void setDigestStringifier(Function<byte[], String> digestStringifier)
    {
        this.digestStringifier = digestStringifier;
    }

    public Map<String, String> getDigestMap()
    {
        return getDigests().entrySet()
                           .stream()
                           .collect(Collectors.toMap(Map.Entry::getKey,
                                                     e -> stringifyDigest(digestStringifier, e.getValue().digest())));
    }

    protected String stringifyDigest(Function<byte[], String> digestStringifier,
                                     byte[] d)
    {
        return digestStringifier.apply(d);
    }
    
    @Override
    public void write(byte[] b)
        throws IOException
    {
        super.write(b);
        cacheOutputStreamTemplate.apply(o -> o.write(b));
    }

    @Override
    public void close()
        throws IOException
    {
        super.close();
        cacheOutputStreamTemplate.apply(o -> o.close());
    }

    @Override
    public void flush()
        throws IOException
    {
        super.flush();
        cacheOutputStreamTemplate.apply(o -> o.flush());
    }

    private Object doWithOutputStream(OutputStreamFunction f)
    {
        if (cacheOutputStream == null)
        {
            return null;
        }
        try
        {
            f.apply(cacheOutputStream);
        }
        catch (IOException t)
        {
            try
            {
                cacheOutputStream.close();
            }
            catch (IOException e)
            {
            }
            cacheOutputStream = null;
        }
        return null;
    }

    @FunctionalInterface
    public interface OutputStreamFunction
    {
        void apply(OutputStream t)
            throws IOException;
    }
}
