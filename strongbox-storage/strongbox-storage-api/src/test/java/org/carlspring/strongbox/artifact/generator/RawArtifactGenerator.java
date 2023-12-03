package org.carlspring.strongbox.artifact.generator;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.carlspring.strongbox.io.LayoutOutputStream;
import org.carlspring.strongbox.testing.artifact.LicenseConfiguration;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.carlspring.strongbox.util.TestFileUtils;

/**
 * @author sbespalov
 *
 */
public class RawArtifactGenerator implements ArtifactGenerator
{

    private final Path baseDir;

    public RawArtifactGenerator()
    {
        this(Paths.get("."));
    }

    public RawArtifactGenerator(Path baseDir)
    {
        this.baseDir = baseDir;
    }

    @Override
    public Path generateArtifact(String id,
                                 String version,
                                 long size)
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path generateArtifact(URI uri,
                                 long bytesSize)
        throws IOException
    {
        Path path = baseDir.resolve(uri.toString());

        return generateArtifact(path, bytesSize);
    }

    private Path generateArtifact(Path path,
                                  long bytesSize)
        throws IOException
    {
        Files.createDirectories(path.getParent());
        try (OutputStream fileOutputStream = Files.newOutputStream(path,
                                                                   StandardOpenOption.TRUNCATE_EXISTING,
                                                                   StandardOpenOption.CREATE);
                LayoutOutputStream layoutOutputStream = new LayoutOutputStream(fileOutputStream))
        {
            layoutOutputStream.addAlgorithm(MessageDigestAlgorithms.MD5);

            TestFileUtils.generateFile(layoutOutputStream, bytesSize);

            generateChecksum(path, layoutOutputStream);

        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IOException(e);
        }

        return path;
    }

    private void generateChecksum(Path artifactPath,
                                  LayoutOutputStream layoutOutputStream)
        throws IOException
    {
        String md5 = layoutOutputStream.getDigestMap().get(MessageDigestAlgorithms.MD5);
        MessageDigestUtils.writeChecksum(artifactPath, ".md5", md5);
    }

    @Override
    public void setLicenses(LicenseConfiguration[] licenses)
    {
        
    }

}
