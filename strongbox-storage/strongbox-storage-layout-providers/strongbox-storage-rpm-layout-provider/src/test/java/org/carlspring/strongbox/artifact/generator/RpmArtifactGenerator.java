package org.carlspring.strongbox.artifact.generator;

import org.carlspring.strongbox.artifact.coordinates.RpmArtifactCoordinates;
import org.carlspring.strongbox.util.TestFileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import org.redline_rpm.Builder;
import static org.redline_rpm.header.Architecture.NOARCH;
import static org.redline_rpm.header.Os.LINUX;
import static org.redline_rpm.header.RpmType.BINARY;


public class RpmArtifactGenerator
        implements ArtifactGenerator
{

    private RpmArtifactCoordinates coordinates;

    private Path basePath;


    public RpmArtifactGenerator(Path basePath)
    {
        super();
        this.basePath = basePath;
    }

    public RpmArtifactGenerator of(RpmArtifactCoordinates coordinates)
    {
        this.coordinates = coordinates;

        return this;
    }

    public RpmArtifactGenerator in(Path path)
    {
        this.basePath = path;
        return this;
    }

    public RpmArtifactCoordinates getCoordinates()
    {
        return coordinates;
    }

    public void setCoordinates(RpmArtifactCoordinates coordinates)
    {
        this.coordinates = coordinates;
    }

    public Path getBasePath()
    {
        return basePath;
    }

    public void setBasePath(Path basePath)
    {
        this.basePath = basePath;
    }

    public Path getPackagePath()
    {
        return Paths.get(coordinates.toPath());
    }

    public void generate(long byteSize)
            throws IOException
    {
        try
        {
            String packagePath = getPackagePath().toString();

            File rpmFile = Paths.get(basePath.toString(), packagePath).toFile();

            if (!rpmFile.getParentFile().exists())
            {
                //noinspection ResultOfMethodCallIgnored
                rpmFile.getParentFile().mkdirs();
            }

            File fileWithRandomSize = new File(basePath.toString(),
                                               "random-sized-file-for-" + packagePath.substring(0, packagePath.length() - 4));

            TestFileUtils.generateFile(fileWithRandomSize, byteSize);

            Builder builder = new Builder();
            builder.setPackage(coordinates.getBaseName(), coordinates.getVersion(), "1");
            builder.setBuildHost("localhost");
            builder.setLicense("GPL");
            builder.setPlatform(NOARCH, LINUX);
            builder.setType(BINARY);
            builder.addFile("etc", fileWithRandomSize);
            builder.build(getBasePath().toFile());

            //noinspection ResultOfMethodCallIgnored
            fileWithRandomSize.delete();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public Path generateArtifact(URI uri,
                                 long bytesSize)
            throws IOException
    {
        RpmArtifactCoordinates coordinates = RpmArtifactCoordinates.parse(uri.toString());

        setCoordinates(coordinates);

        generate(bytesSize);

        return Paths.get(basePath.toString(), coordinates.toPath());
    }

    @Override
    public Path generateArtifact(String id,
                                 String version,
                                 long bytesSize)
            throws IOException
    {
        generate(bytesSize);

        return Paths.get(basePath.toString(), getPackagePath().toString());
    }

}
