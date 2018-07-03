package org.carlspring.strongbox.artifact.coordinates;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import ru.aristar.jnuget.Version;
import ru.aristar.jnuget.files.Framework;
import ru.aristar.jnuget.files.Hash;
import ru.aristar.jnuget.files.NugetFormatException;
import ru.aristar.jnuget.files.Nupkg;
import ru.aristar.jnuget.files.nuspec.NuspecFile;
import ru.aristar.jnuget.files.nuspec.NuspecFile.Metadata;

public class PathNupkg implements Nupkg
{

    private static final Logger logger = LoggerFactory.getLogger(PathNupkg.class);

    private RepositoryPath path;
    private NuspecFile nuspecFile;
    private Hash hash;
    private NugetArtifactCoordinates artifactCoordinates;
    private boolean exists;

    public PathNupkg(RepositoryPath path)
        throws NugetFormatException,
        UnsupportedEncodingException,
        IOException
    {
        Assert.notNull(path);
        Assert.notNull(path.getArtifactEntry());
        
        this.path = path;
        this.artifactCoordinates = (NugetArtifactCoordinates) path.getArtifactEntry().getArtifactCoordinates();
        this.nuspecFile = createNuspecFile();
        this.hash = createHash();
    }

    public RepositoryPath getPath()
    {
        return path;
    }

    @Override
    public String getFileName()
    {
        return path.getFileName().toString();
    }

    @Override
    public Hash getHash()
        throws NoSuchAlgorithmException,
        IOException
    {
        return hash;
    }

    private Hash createHash()
        throws IOException,
        UnsupportedEncodingException
    {
        Map<String, RepositoryPath> checksumPathMap = path.getFileSystem().provider().resolveChecksumPathMap(path);
        if (checksumPathMap.isEmpty())
        {
            return null;
        }
        // Nuget package should have only one checksum digest algorithm.
        RepositoryPath checkSumPath = checksumPathMap.values().iterator().next();
        if (!Files.exists(checkSumPath))
        {
            logger.trace(String.format("Failed to resolve checksum file for [%s]", path));
            return new Hash(new byte[] {});
        }
        List<String> checkSumContents = Files.readAllLines(checkSumPath);
        if (checkSumContents.isEmpty() || checkSumContents.size() > 1)
        {
            logger.error(String.format("Found illegal checksum contents for [%s]", path));
            return null;
        }
        String checkSumStr = checkSumContents.iterator().next();
        return new Hash(Base64.getDecoder().decode(checkSumStr.getBytes("UTF-8")));
    }

    @Override
    public NuspecFile getNuspecFile()
        throws NugetFormatException
    {
        return nuspecFile;
    }

    private NuspecFile createNuspecFile()
        throws NugetFormatException
    {
        RepositoryPath nuspecPath = path.resolveSibling(artifactCoordinates.getId() + ".nuspec");
        if (!Files.exists(nuspecPath))
        {
            logger.trace(String.format("Failed to resolve .nuspec file for [%s]", path));
            NuspecFile result = new NuspecFile();
            Metadata metadata = result.getMetadata();
            metadata.id = artifactCoordinates.getId();
            metadata.version = Version.parse(artifactCoordinates.getVersion());
            metadata.title = metadata.id;
            return result;
        }
        exists = true;
        try
        {
            return NuspecFile.Parse(Files.newInputStream(nuspecPath));
        }
        catch (IOException e)
        {
            logger.error(String.format("Failed to read .nuspec file for [%s]", path), e);
            return null;
        }
    }

    @Override
    public Long getSize()
    {
        try
        {
            return path.getArtifactEntry().getSizeInBytes();
        }
        catch (IOException e)
        {
            return 0L;
        }
    }

    @Override
    public InputStream getStream()
        throws IOException
    {
        return Files.newInputStream(path);
    }

    @Override
    public Date getUpdated()
    {
        try
        {
            return path.getArtifactEntry().getLastUpdated();
        }
        catch (IOException e)
        {
            return null;
        }
    }

    @Override
    public String getId()
    {
        return artifactCoordinates.getId();
    }

    @Override
    public Version getVersion()
    {
        try
        {
            return Version.parse(artifactCoordinates.getVersion());
        }
        catch (NugetFormatException e)
        {
            logger.error(String.format("Failed to parse version for [%s]", path));
            return null;
        }
    }

    @Override
    public EnumSet<Framework> getTargetFramework()
    {
        // TODO: add framework into RepositoryPath attributes for nuget packages.
        return EnumSet.allOf(Framework.class);
    }

    @Override
    public void load()
        throws IOException
    {
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof PathNupkg)) {
            return false;
        }
        return path.equals(((PathNupkg)obj).path);
    }

    @Override
    public int hashCode()
    {
        return path.hashCode();
    }

}
