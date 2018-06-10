package org.carlspring.strongbox.storage.repository.remote;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class RemoteRepository
{

    private final boolean downloadRemoteIndexes;

    private final boolean autoBlocking;

    private final boolean checksumValidation;

    private final String username;

    private final String password;

    private final String checksumPolicy;

    private final Integer checkIntervalSeconds;

    private final boolean allowsDirectoryBrowsing;

    private final boolean autoImportRemoteSSLCertificate;
    
    private String url;

    public RemoteRepository(final MutableRemoteRepository other)
    {
        this.url = other.getUrl();
        this.downloadRemoteIndexes = other.isDownloadRemoteIndexes();
        this.autoBlocking = other.isAutoBlocking();
        this.checksumValidation = other.isChecksumValidation();
        this.username = other.getUsername();
        this.password = other.getPassword();
        this.checksumPolicy = other.getChecksumPolicy();
        this.checkIntervalSeconds = other.getCheckIntervalSeconds();
        this.allowsDirectoryBrowsing = other.allowsDirectoryBrowsing();
        this.autoImportRemoteSSLCertificate = other.isAutoImportRemoteSSLCertificate();
    }

    public String getUrl()
    {
        return url;
    }

    public boolean isDownloadRemoteIndexes()
    {
        return downloadRemoteIndexes;
    }

    public boolean isAutoBlocking()
    {
        return autoBlocking;
    }

    public boolean isChecksumValidation()
    {
        return checksumValidation;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getChecksumPolicy()
    {
        return checksumPolicy;
    }

    public Integer getCheckIntervalSeconds()
    {
        return checkIntervalSeconds;
    }

    public boolean allowsDirectoryBrowsing()
    {
        return allowsDirectoryBrowsing;
    }

    public boolean isAutoImportRemoteSSLCertificate()
    {
        return autoImportRemoteSSLCertificate;
    }
}
