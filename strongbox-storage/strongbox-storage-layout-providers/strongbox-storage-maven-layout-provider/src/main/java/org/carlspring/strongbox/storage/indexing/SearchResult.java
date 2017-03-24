package org.carlspring.strongbox.storage.indexing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "artifact")
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchResult
{

    @XmlElement
    private String groupId;

    @XmlElement
    private String artifactId;

    @XmlElement
    private String version;

    @XmlElement
    private String classifier;

    @XmlTransient
    private String extension;

    @XmlElement
    private String storageId;

    @XmlElement
    private String repositoryId;

    @XmlElement
    private String path;

    @XmlElement
    private String url;


    public SearchResult()
    {
    }

    public SearchResult(String storageId,
                        String repositoryId,
                        String groupId,
                        String artifactId,
                        String version,
                        String classifier,
                        String extension,
                        String path,
                        String url)
    {
        this.storageId = storageId;
        this.repositoryId = repositoryId;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.extension = extension;
        this.path = path;
        this.url = url;
    }

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public void setClassifier(String classifier)
    {
        this.classifier = classifier;
    }

    public String getExtension()
    {
        return extension;
    }

    public void setExtension(String extension)
    {
        this.extension = extension;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @Override
    public String toString()
    {
        return groupId + ':' + artifactId + ':' + version + ':' + extension + ':' + classifier;
    }

}
