package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

import java.net.URL;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;

import static org.carlspring.strongbox.providers.io.RepositoryFileAttributeType.*;

public class RepositoryFileAttributes
        implements BasicFileAttributes
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

    protected void setCoordinates(ArtifactCoordinates coordinates)
    {
        attributes.put(COORDINATES, coordinates);
    }

    public boolean isMetadata()
    {
        return Boolean.TRUE.equals(attributes.get(METADATA));
    }

    protected void setMetadata(boolean isMetadata)
    {
        attributes.put(METADATA, isMetadata);
    }

    public boolean isChecksum()
    {
        return Boolean.TRUE.equals(attributes.get(CHECKSUM));
    }

    protected void setChecksum(boolean isChecksum)
    {
        attributes.put(CHECKSUM, isChecksum);
    }

    public boolean isTrash()
    {
        return Boolean.TRUE.equals(attributes.get(TRASH));
    }

    protected void setTrash(boolean isTrash)
    {
        attributes.put(TRASH, isTrash);
    }

    public boolean isTemp()
    {
        return Boolean.TRUE.equals(attributes.get(TEMP));
    }

    protected void setTemp(boolean isTemp)
    {
        attributes.put(TEMP, isTemp);
    }

    public boolean isArtifact()
    {
        return Boolean.TRUE.equals(attributes.get(ARTIFACT));
    }

    protected void setArtifact(boolean isArtifact)
    {
        attributes.put(RepositoryFileAttributeType.ARTIFACT, isArtifact);
    }

    public boolean hasExpired()
    {
        return Boolean.TRUE.equals(attributes.get(EXPIRED));
    }

    public boolean getArtifactPath()
    {
        return Boolean.TRUE.equals(attributes.get(ARTIFACT_PATH));
    }

    protected void setArtifactPath(String path)
    {
        attributes.put(ARTIFACT_PATH, path);
    }

    public String getStorageId()
    {
        return (String) attributes.get(STORAGE_ID);
    }

    protected void setStorageId(String id)
    {
        attributes.put(STORAGE_ID, id);
    }

    public String getRepositoryId()
    {
        return (String) attributes.get(REPOSITORY_ID);
    }

    public void setRepositoryId(String id)
    {
        attributes.put(REPOSITORY_ID, id);
    }

}
