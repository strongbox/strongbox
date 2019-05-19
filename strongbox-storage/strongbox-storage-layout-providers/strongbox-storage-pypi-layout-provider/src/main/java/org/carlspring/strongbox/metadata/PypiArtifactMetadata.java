package org.carlspring.strongbox.metadata;

/**
 * @author carlspring
 */
public class PypiArtifactMetadata
{

    private String name;

    private String platform;

    private String version;

    private String summary;

    private String description;

    private String md5Digest;

    private String license;

    private String pythonVersion;

    private String comment;

    private String author;

    private String authorEmail;

    private String protocolVersion;

    private String fileType;

    private String metadataVersion;

    private String homePage;

    private String downloadUrl;


    public PypiArtifactMetadata()
    {
    }

    public PypiArtifactMetadata(String name,
                                String platform,
                                String version,
                                String summary,
                                String description,
                                String md5Digest,
                                String license,
                                String pythonVersion,
                                String comment,
                                String author,
                                String authorEmail,
                                String protocolVersion,
                                String fileType,
                                String metadataVersion,
                                String homePage,
                                String downloadUrl)
    {
        this.name = name;
        this.platform = platform;
        this.version = version;
        this.summary = summary;
        this.description = description;
        this.md5Digest = md5Digest;
        this.license = license;
        this.pythonVersion = pythonVersion;
        this.comment = comment;
        this.author = author;
        this.authorEmail = authorEmail;
        this.protocolVersion = protocolVersion;
        this.fileType = fileType;
        this.metadataVersion = metadataVersion;
        this.homePage = homePage;
        this.downloadUrl = downloadUrl;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPlatform()
    {
        return platform;
    }

    public void setPlatform(String platform)
    {
        this.platform = platform;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getMd5Digest()
    {
        return md5Digest;
    }

    public void setMd5Digest(String md5Digest)
    {
        this.md5Digest = md5Digest;
    }

    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    public String getPythonVersion()
    {
        return pythonVersion;
    }

    public void setPythonVersion(String pythonVersion)
    {
        this.pythonVersion = pythonVersion;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getAuthorEmail()
    {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail)
    {
        this.authorEmail = authorEmail;
    }

    public String getProtocolVersion()
    {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion)
    {
        this.protocolVersion = protocolVersion;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public String getMetadataVersion()
    {
        return metadataVersion;
    }

    public void setMetadataVersion(String metadataVersion)
    {
        this.metadataVersion = metadataVersion;
    }

    public String getHomePage()
    {
        return homePage;
    }

    public void setHomePage(String homePage)
    {
        this.homePage = homePage;
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }

}
