package org.carlspring.strongbox.storage.indexing;

import javax.xml.bind.annotation.*;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "artifacts")
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
    private String repository;

    @XmlElement
    private String path;

    @XmlElement
    private String url;


    public SearchResult()
    {
    }

    public SearchResult(String repository,
                        String groupId,
                        String artifactId,
                        String version,
                        String classifier,
                        String extension,
                        String path,
                        String url)
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.extension = extension;
        this.repository = repository;
        this.path = path;
        this.url = url;
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

    public String getRepository()
    {
        return repository;
    }

    public void setRepository(String repository)
    {
        this.repository = repository;
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
        return groupId + ':' + artifactId + ':' + version + ':' + extension + ':' + classifier ;
    }

}
