package org.carlspring.strongbox.storage.repository.remote;

import static org.carlspring.strongbox.configuration.MutableRemoteRepositoriesConfiguration.DEFAULT_HEARTBEAT_INTERVAL_SECONDS;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.carlspring.strongbox.xml.repository.remote.MutableRemoteRepositoryConfiguration;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "remote-repository")
@XmlAccessorType(XmlAccessType.FIELD)
public class MutableRemoteRepository
        implements Serializable
{

    @XmlAttribute
    private String url;

    @XmlAttribute(name = "download-remote-indexes")
    private boolean downloadRemoteIndexes;

    @XmlAttribute(name = "auto-blocking")
    private boolean autoBlocking;

    @XmlAttribute(name = "checksum-validation")
    private boolean checksumValidation;

    @XmlAttribute
    private String username;

    @XmlAttribute
    private String password;

    @XmlAttribute(name = "checksum-policy")
    private String checksumPolicy;

    @XmlAttribute(name = "check-interval-seconds")
    private Integer checkIntervalSeconds = DEFAULT_HEARTBEAT_INTERVAL_SECONDS;

    @XmlAttribute(name = "allows-directory-browsing")
    private boolean allowsDirectoryBrowsing = true;

    @XmlAttribute(name = "auto-import-remote-ssl-certificate")
    private boolean autoImportRemoteSSLCertificate;

    @XmlElementRef
    private MutableRemoteRepositoryConfiguration customConfiguration;
    
    public MutableRemoteRepository()
    {
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public boolean isDownloadRemoteIndexes()
    {
        return downloadRemoteIndexes;
    }

    public void setDownloadRemoteIndexes(boolean downloadRemoteIndexes)
    {
        this.downloadRemoteIndexes = downloadRemoteIndexes;
    }

    public boolean isAutoBlocking()
    {
        return autoBlocking;
    }

    public void setAutoBlocking(boolean autoBlocking)
    {
        this.autoBlocking = autoBlocking;
    }

    public boolean isChecksumValidation()
    {
        return checksumValidation;
    }

    public void setChecksumValidation(boolean checksumValidation)
    {
        this.checksumValidation = checksumValidation;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getChecksumPolicy()
    {
        return checksumPolicy;
    }

    public void setChecksumPolicy(String checksumPolicy)
    {
        this.checksumPolicy = checksumPolicy;
    }

    public Integer getCheckIntervalSeconds()
    {
        return checkIntervalSeconds;
    }

    public void setCheckIntervalSeconds(Integer checkIntervalSeconds)
    {
        this.checkIntervalSeconds = checkIntervalSeconds;
    }

    public void setAllowsDirectoryBrowsing(boolean allowsDirectoryBrowsing)
    {
        this.allowsDirectoryBrowsing = allowsDirectoryBrowsing;
    }

    public boolean isAutoImportRemoteSSLCertificate()
    {
        return autoImportRemoteSSLCertificate;
    }

    public void setAutoImportRemoteSSLCertificate(boolean autoImportRemoteSSLCertificate)
    {
        this.autoImportRemoteSSLCertificate = autoImportRemoteSSLCertificate;
    }

    public boolean allowsDirectoryBrowsing()
    {
        return allowsDirectoryBrowsing;
    }

    public MutableRemoteRepositoryConfiguration getCustomConfiguration()
    {
        return customConfiguration;
    }

    public void setCustomConfiguration(MutableRemoteRepositoryConfiguration customConfiguration)
    {
        this.customConfiguration = customConfiguration;
    }
    
}
