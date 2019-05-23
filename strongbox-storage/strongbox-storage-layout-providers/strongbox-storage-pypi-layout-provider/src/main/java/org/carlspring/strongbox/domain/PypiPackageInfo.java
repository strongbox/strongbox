package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.util.annotations.PypiMetadataKey;

public class PypiPackageInfo
{

    @PypiMetadataKey(name = "Metadata-Version")
    private String metadataVersion;

    @PypiMetadataKey(name = "Name")
    private String name;

    @PypiMetadataKey(name = "Version")
    private String version;

    @PypiMetadataKey(name = "Summary")
    private String summary;

    @PypiMetadataKey(name = "Home-page")
    private String homePage;

    @PypiMetadataKey(name = "Author")
    private String author;

    @PypiMetadataKey(name = "Author-email")
    private String authorEmail;

    @PypiMetadataKey(name = "License")
    private String license;

    @PypiMetadataKey(name = "Description-Content-Type")
    private String descriptionContentType;

    @PypiMetadataKey(name = "Description")
    private String description;

    @PypiMetadataKey(name = "Platform")
    private String platform;
    
    
    public PypiPackageInfo()
    {
    }

    public String getMetadataVersion()
    {
        return metadataVersion;
    }

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return version;
    }

    public String getSummary()
    {
        return summary;
    }

    public String getHomePage()
    {
        return homePage;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getAuthorEmail()
    {
        return authorEmail;
    }

    public String getLicense()
    {
        return license;
    }

    public String getDescriptionContentType()
    {
        return descriptionContentType;
    }

    public String getDescription()
    {
        return description;
    }

    public String getPlatform()
    {
        return platform;
    }

}
