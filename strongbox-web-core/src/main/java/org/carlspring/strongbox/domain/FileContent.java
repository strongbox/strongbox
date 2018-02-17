package org.carlspring.strongbox.domain;

public class FileContent 
{
    private String name;

    private String size;

    private String lastModified;
    
    private String storageId;
    
    private String repositoryId;
    
    private String artifactPath;

    public String getName()
    {
        return name;
    }

    public String getSize()
    {
        return size;
    }

    public String getLastModified()
    {
        return lastModified;
    }

    public String getStorageId()
    {
        return storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public String getArtifactPath()
    {
        return artifactPath;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setSize(String size)
    {
        this.size = size;
    }

    public void setLastModified(String lastModified)
    {
        this.lastModified = lastModified;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public void setArtifactPath(String artifactPath)
    {
        this.artifactPath = artifactPath;
    }   
}

