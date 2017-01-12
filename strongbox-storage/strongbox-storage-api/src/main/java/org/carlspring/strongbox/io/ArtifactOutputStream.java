package org.carlspring.strongbox.io;

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
    
    public ArtifactOutputStream(OutputStream source,
                                ArtifactCoordinates coordinates)
        throws NoSuchAlgorithmException
    {
        super(source, new String[] {});
        this.coordinates = coordinates;
    }

    public ArtifactCoordinates getCoordinates()
    {
        return coordinates;
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
}
