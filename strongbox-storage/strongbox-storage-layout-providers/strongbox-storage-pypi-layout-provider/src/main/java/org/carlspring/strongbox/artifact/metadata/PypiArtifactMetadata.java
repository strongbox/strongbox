package org.carlspring.strongbox.artifact.metadata;

/**
 * 
 * @author ankit.tomar
 *
 */
public class PypiArtifactMetadata
{

    private String comment = null;

    private String metdataVersion = null;

    private String fileType = null;

    private String protcolVersion = null;

    private String author = null;

    private String homePage = null;

    private String downloadUrl = null;

    private String platform = null;

    private String version = null;

    private String description = null;

    private String md5Digest = null;

    private String action = null;

    private String name = null;

    private String license = null;

    private String pyVersion = null;

    private String summary = null;

    private String authorEmail = null;

    public String getComment()
    {
        return comment;
    }

    public PypiArtifactMetadata setComment(String comment)
    {
        this.comment = comment;
        return this;
    }

    public String getMetdataVersion()
    {
        return metdataVersion;
    }

    public PypiArtifactMetadata setMetdataVersion(String metdataVersion)
    {
        this.metdataVersion = metdataVersion;
        return this;
    }

    public String getFileType()
    {
        return fileType;
    }

    public PypiArtifactMetadata setFileType(String fileType)
    {
        this.fileType = fileType;
        return this;
    }

    public String getProtcolVersion()
    {
        return protcolVersion;
    }

    public PypiArtifactMetadata setProtcolVersion(String protcolVersion)
    {
        this.protcolVersion = protcolVersion;
        return this;
    }

    public String getAuthor()
    {
        return author;
    }

    public PypiArtifactMetadata setAuthor(String author)
    {
        this.author = author;
        return this;
    }

    public String getHomePage()
    {
        return homePage;
    }

    public PypiArtifactMetadata setHomePage(String homePage)
    {
        this.homePage = homePage;
        return this;
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    public PypiArtifactMetadata setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
        return this;
    }

    public String getPlatform()
    {
        return platform;
    }

    public PypiArtifactMetadata setPlatform(String platform)
    {
        this.platform = platform;
        return this;
    }

    public String getVersion()
    {
        return version;
    }

    public PypiArtifactMetadata setVersion(String version)
    {
        this.version = version;
        return this;
    }

    public String getDescription()
    {
        return description;
    }

    public PypiArtifactMetadata setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public String getMd5Digest()
    {
        return md5Digest;
    }

    public PypiArtifactMetadata setMd5Digest(String md5Digest)
    {
        this.md5Digest = md5Digest;
        return this;
    }

    public String getAction()
    {
        return action;
    }

    public PypiArtifactMetadata setAction(String action)
    {
        this.action = action;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public PypiArtifactMetadata setName(String name)
    {
        this.name = name;
        return this;
    }

    public String getLicense()
    {
        return license;
    }

    public PypiArtifactMetadata setLicense(String license)
    {
        this.license = license;
        return this;
    }

    public String getPyVersion()
    {
        return pyVersion;
    }

    public PypiArtifactMetadata setPyVersion(String pyVersion)
    {
        this.pyVersion = pyVersion;
        return this;
    }

    public String getSummary()
    {
        return summary;
    }

    public PypiArtifactMetadata setSummary(String summary)
    {
        this.summary = summary;
        return this;
    }

    public String getAuthorEmail()
    {
        return authorEmail;
    }

    public PypiArtifactMetadata setAuthorEmail(String authorEmail)
    {
        this.authorEmail = authorEmail;
        return this;
    }

    @Override
    public String toString()
    {
        return "PypiArtifactMetadata [comment=" + comment + ", metdataVersion=" + metdataVersion + ", fileType="
                + fileType + ", protcolVersion=" + protcolVersion + ", author=" + author + ", homePage=" + homePage
                + ", downloadUrl=" + downloadUrl + ", platform=" + platform + ", version=" + version + ", description="
                + description + ", md5Digest=" + md5Digest + ", action=" + action + ", name=" + name + ", license="
                + license + ", pyVersion=" + pyVersion + ", summary=" + summary + ", authorEmail=" + authorEmail + "]";
    }
    
    

}
