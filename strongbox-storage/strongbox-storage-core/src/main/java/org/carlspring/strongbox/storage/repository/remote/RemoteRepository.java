package org.carlspring.strongbox.storage.repository.remote;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.carlspring.strongbox.xml.repository.remote.CustomRemoteRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.remote.MutableRemoteRepositoryConfiguration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressFBWarnings(value = "AJCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
public class RemoteRepository
{

    private boolean downloadRemoteIndexes;

    private boolean autoBlocking;

    private boolean checksumValidation;

    private String username;

    private String password;

    private String checksumPolicy;

    private Integer checkIntervalSeconds;

    private boolean allowsDirectoryBrowsing;

    private boolean autoImportRemoteSSLCertificate;

    private String url;
    
    private CustomRemoteRepositoryConfiguration customConfiguration;

    RemoteRepository()
    {

    }

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
        this.customConfiguration = immuteRemoteRepositoryConfiguration(other.getCustomConfiguration());
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

    public CustomRemoteRepositoryConfiguration getCustomConfiguration()
    {
        return customConfiguration;
    }

    private CustomRemoteRepositoryConfiguration immuteRemoteRepositoryConfiguration(final MutableRemoteRepositoryConfiguration source)
    {
        return source != null ? source.getImmutable() : null;
    }

}
