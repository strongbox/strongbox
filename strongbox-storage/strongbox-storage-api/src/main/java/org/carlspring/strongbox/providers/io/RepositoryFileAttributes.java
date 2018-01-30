package org.carlspring.strongbox.providers.io;

import static org.carlspring.strongbox.providers.io.RepositoryFileAttributeType.*;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

public class RepositoryFileAttributes implements BasicFileAttributes
{

    private BasicFileAttributes basicAttributes;

    private Map<RepositoryFileAttributeType, Object> attributes = new HashMap<>();

    public RepositoryFileAttributes(BasicFileAttributes basicAttributes)
    {
        super();
        this.basicAttributes = basicAttributes;
    }

    public RepositoryFileAttributes(BasicFileAttributes basicAttributes,
                                    Map<RepositoryFileAttributeType, Object> attributes)
    {
        super();
        this.basicAttributes = basicAttributes;
        this.attributes = attributes;
    }

    public FileTime lastModifiedTime()
    {
        return basicAttributes.lastModifiedTime();
    }

    public FileTime lastAccessTime()
    {
        return basicAttributes.lastAccessTime();
    }

    public FileTime creationTime()
    {
        return basicAttributes.creationTime();
    }

    public boolean isRegularFile()
    {
        return basicAttributes.isRegularFile();
    }

    public boolean isDirectory()
    {
        return basicAttributes.isDirectory();
    }

    public boolean isSymbolicLink()
    {
        return basicAttributes.isSymbolicLink();
    }

    public boolean isOther()
    {
        return basicAttributes.isOther();
    }

    public long size()
    {
        return basicAttributes.size();
    }

    public Object fileKey()
    {
        return basicAttributes.fileKey();
    }

    public ArtifactCoordinates getCoordinates()
    {
        return (ArtifactCoordinates) attributes.get(COORDINATES);
    }

    public boolean isMetadata()
    {
        return Boolean.TRUE.equals(attributes.get(METADATA));
    }

    public boolean isChecksum()
    {
        return Boolean.TRUE.equals(attributes.get(CHECKSUM));
    }

    public boolean isTrash()
    {
        return Boolean.TRUE.equals(attributes.get(TRASH));
    }

    public boolean isTemp()
    {
        return Boolean.TRUE.equals(attributes.get(TEMP));
    }

    public boolean isIndex()
    {
        return Boolean.TRUE.equals(attributes.get(INDEX));
    }

    // TODO: we should determine real platform specific metadata files as
    // artifacts too, so files like `pom`|`nuspec`|`package.json` should be
    // treated as artifacts and they should have an ArtifactCoodrinates (for now they have no coordinates)
    // also we need special attribute for files like maven-metadata.xml, it can be called as `other`
    public boolean isArtifact()
    {
        return Boolean.TRUE.equals(attributes.get(ARTIFACT));
    }

    protected void setMetadata(boolean isMetadata)
    {
        attributes.put(METADATA, isMetadata);
    }

    protected void setCoordinates(ArtifactCoordinates coordinates)
    {
        attributes.put(COORDINATES, coordinates);
    }

    protected void setChecksum(boolean isChecksum)
    {
        attributes.put(CHECKSUM, isChecksum);
    }

    protected void setTrash(boolean isTrash)
    {
        attributes.put(TRASH, isTrash);
    }

    protected void setTemp(boolean isTemp)
    {
        attributes.put(TEMP, isTemp);
    }

    protected void setIndex(boolean isIndex)
    {
        attributes.put(INDEX, isIndex);
    }

    protected void setArtifact(boolean isArtifact)
    {
        attributes.put(ARTIFACT, isArtifact);
    }

}