package org.carlspring.strongbox.artifact.generator;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.carlspring.commons.io.RandomInputStream;
import org.carlspring.strongbox.io.LayoutOutputStream;

/**
 * @author sbespalov
 *
 */
public class NullArtifactGenerator implements ArtifactGenerator
{

    private final Path baseDir;

    public NullArtifactGenerator()
    {
        this(Paths.get("."));
    }

    public NullArtifactGenerator(Path baseDir)
    {
        this.baseDir = baseDir;
    }

    @Override
    public Path generateArtifact(String id,
                                 String version,
                                 int size)
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path generateArtifact(URI uri,
                                 int size)
        throws IOException
    {
        Path path = baseDir.resolve(uri.toString());
        
        return generateArtifact(size, path);
    }

    private Path generateArtifact(int size,
                                  Path path)
        throws IOException
    {
        Files.createDirectories(path.getParent());
        try (OutputStream fileOutputStream = Files.newOutputStream(path,
                                                                   StandardOpenOption.TRUNCATE_EXISTING,
                                                                   StandardOpenOption.CREATE);
                LayoutOutputStream layoutOutputStream = new LayoutOutputStream(fileOutputStream);
                RandomInputStream ris = new RandomInputStream(size))
        {
            layoutOutputStream.addAlgorithm(MessageDigestAlgorithms.MD5);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = ris.read(buffer)) > 0)
            {
                layoutOutputStream.write(buffer, 0, len);
            }

            layoutOutputStream.flush();
            layoutOutputStream.getDigestMap()
                              .entrySet()
                              .stream()
                              .forEach(e -> writeChecksum(path, e.getKey(), e.getValue()));

        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IOException(e);
        }

        return path;
    }

    private void writeChecksum(Path path,
                               String algorithm,
                               String value)
    {
        String fileName = path.getFileName().toString();
        String checksumFileName = fileName + "." + algorithm.toLowerCase();
        try
        {
            Files.write(path.resolveSibling(checksumFileName),
                        value.getBytes(),
                        StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }
    }

}
