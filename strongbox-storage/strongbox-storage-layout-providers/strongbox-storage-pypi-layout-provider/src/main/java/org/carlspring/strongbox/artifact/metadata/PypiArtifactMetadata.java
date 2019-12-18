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

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public String getMetdataVersion()
    {
        return metdataVersion;
    }

    public void setMetdataVersion(String metdataVersion)
    {
        this.metdataVersion = metdataVersion;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public String getProtcolVersion()
    {
        return protcolVersion;
    }

    public void setProtcolVersion(String protcolVersion)
    {
        this.protcolVersion = protcolVersion;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
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

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    public String getPyVersion()
    {
        return pyVersion;
    }

    public void setPyVersion(String pyVersion)
    {
        this.pyVersion = pyVersion;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getAuthorEmail()
    {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail)
    {
        this.authorEmail = authorEmail;
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
