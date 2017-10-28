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

import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        this.path = path;
        this.artifactCoordinates = readArtifactCooridnates();
        this.nuspecFile = createNuspecFile();
        this.hash = createHash();
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
            logger.debug(String.format("Failed to resolve checksum file for [%s]", path));
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
        NugetArtifactCoordinates artifactCoordinates = readArtifactCooridnates();
        RepositoryPath parentPath = path.getParent();
        RepositoryPath nuspecPath = parentPath.resolve(artifactCoordinates.getId() + ".nuspec");
        if (!Files.exists(nuspecPath))
        {
            logger.debug(String.format("Failed to resolve .nuspec file for [%s]", path));
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

    private NugetArtifactCoordinates readArtifactCooridnates()
    {
        NugetArtifactCoordinates artifactCoordinates;
        try
        {
            artifactCoordinates = (NugetArtifactCoordinates) Files.getAttribute(path,
                                                                                RepositoryFileAttributes.COORDINATES);
        }
        catch (IOException e)
        {
            logger.error(String.format("Failed to read ArtifactCoordinates for [%s]", path), e);
            return null;
        }
        return artifactCoordinates;
    }

    @Override
    public Long getSize()
    {
        if (!exists)
        {
            return -1L;
        }
        try
        {
            return Files.size(path);
        }
        catch (IOException e)
        {
            logger.error(String.format("Failed to calculate file length for [%s]", path), e);
            return null;
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
        if (!exists)
        {
            return null;
        }
        BasicFileAttributes attributes;
        try
        {
            attributes = Files.readAttributes(path, BasicFileAttributes.class);
        }
        catch (IOException e)
        {
            logger.error(String.format("Failed to read file attributes for [%s]", path), e);
            return null;
        }
        return new Date(attributes.lastModifiedTime().toMillis());
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

}
