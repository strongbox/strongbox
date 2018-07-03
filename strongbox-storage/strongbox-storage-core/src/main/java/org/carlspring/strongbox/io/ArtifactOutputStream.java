package org.carlspring.strongbox.io;

import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.util.MessageDigestUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This OutputStream wraps a source stream from different Storage types (File System, AWS, JDBC, etc.).
 * Note that this class is "abstract" and you don't need to instantiate it directly, see example below:
 * <pre>
 * ...
 * RepositoryPath repositoryPath = layoutProvider.resolve("path/to/your/artifact/file.ext");
 * ArtifactOutputStream aos = (ArtifactOutputStream) Files.newOutputStream(repositoryPath); 
 * ...
 * </pre>
 * @author Sergey Bespalov
 */
public class ArtifactOutputStream extends MultipleDigestOutputStream
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactOutputStream.class);

    private Function<byte[], String> digestStringifier = MessageDigestUtils::convertToHexadecimalString;
    private ArtifactCoordinates coordinates;
    /**
     * Used to cache source {@link OutputStream} contents if needed.
     */
    private OutputStream cacheOutputStream;
    private Function<OutputStreamFunction, ?> cacheOutputStreamTemplate = this::doWithOutputStream;

    public ArtifactOutputStream(OutputStream source,
                                ArtifactCoordinates coordinates)
            throws NoSuchAlgorithmException
    {
        super(new BufferedOutputStream(source), new String[]{});
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
    public void write(int b)
        throws IOException
    {
        super.write(b);
        cacheOutputStreamTemplate.apply(o -> o.write(b));
    }

    @Override
    public void write(byte[] b,
                      int off,
                      int len)
        throws IOException
    {
        super.write(b, off, len);
        cacheOutputStreamTemplate.apply(o -> o.write(b, off, len));
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
            logger.error(String.format("Failed to operate with additional artifact stream: coordinates-[%s]",
                                       coordinates),
                         t);
            try
            {
                cacheOutputStream.close();
            }
            catch (IOException e)
            {
                //Do noting here
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
