package org.carlspring.strongbox.providers.io;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

public class RepositoryFileAttributes implements BasicFileAttributes
{

    public static final String COORDINATES = "coordinates";
    public static final String METEDATA = "metedata";
    public static final String CHECKSUM = "checksum";
    public static final String TRASH = "trash";
    public static final String TEMP = "temp";
    public static final String INDEX = "index";
    public static final String ARTIFACT = "artifact";
    public static final String SERVICEFOLDER = "servicefolder";
    
    private BasicFileAttributes basicAttributes;
    private Map<String, Object> attributes = new HashMap<>();

    public RepositoryFileAttributes(BasicFileAttributes basicAttributes)
    {
        super();
        this.basicAttributes = basicAttributes;
    }
    
    public RepositoryFileAttributes(BasicFileAttributes basicAttributes,
                                    Map<String, Object> attributes)
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
        return Boolean.TRUE.equals(attributes.get(METEDATA));
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

    public boolean isArtifact()
    {
        return Boolean.TRUE.equals(attributes.get(ARTIFACT));
    }

    protected void setMetedata(boolean isMetedata)
    {
        attributes.put(METEDATA, isMetedata);
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