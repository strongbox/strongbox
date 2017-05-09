package org.carlspring.strongbox.providers.io;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

public class RepositoryFileAttributes implements BasicFileAttributes
{

    public static final String COORDINATES = "coordinates";
    public static final String IS_METEDATA = "isMetedata";
    public static final String IS_CHECKSUM = "isChecksum";
    public static final String IS_TRASH = "isTrash";
    public static final String IS_TEMP = "isTemp";
    public static final String IS_INDEX = "isIndex";
    public static final String IS_ARTIFACT = "isArtifact";
    public static final String IS_SERVICEFOLDER = "isServicefolder";
    
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

    public boolean getIsMetadata()
    {
        return Boolean.TRUE.equals(attributes.get(IS_METEDATA));
    }

    public boolean getIsChecksum()
    {
        return Boolean.TRUE.equals(attributes.get(IS_CHECKSUM));
    }

    public boolean getIsTrash()
    {
        return Boolean.TRUE.equals(attributes.get(IS_TRASH));
    }

    public boolean getIsTemp()
    {
        return Boolean.TRUE.equals(attributes.get(IS_TEMP));
    }

    public boolean getIsIndex()
    {
        return Boolean.TRUE.equals(attributes.get(IS_INDEX));
    }

    public boolean getIsArtifact()
    {
        return Boolean.TRUE.equals(attributes.get(IS_ARTIFACT));
    }

    protected void setIsMetedata(boolean isMetedata)
    {
        attributes.put(IS_METEDATA, isMetedata);
    }

    protected void setCoordinates(ArtifactCoordinates coordinates)
    {
        attributes.put(COORDINATES, coordinates);
    }

    protected void setIsChecksum(boolean isChecksum)
    {
        attributes.put(IS_CHECKSUM, isChecksum);
    }

    protected void setIsTrash(boolean isTrash)
    {
        attributes.put(IS_TRASH, isTrash);
    }

    protected void setIsTemp(boolean isTemp)
    {
        attributes.put(IS_TEMP, isTemp);
    }

    protected void setIsIndex(boolean isIndex)
    {
        attributes.put(IS_INDEX, isIndex);
    }

    protected void setIsArtifact(boolean isArtifact)
    {
        attributes.put(IS_ARTIFACT, isArtifact);
    }

}