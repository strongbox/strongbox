package org.carlspring.strongbox.storage.repository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "remote-repository")
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteRepository
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

    @XmlAttribute(name = "timeout")
    private long timeout;

    @XmlAttribute(name = "retries")
    private int retries;

    @XmlAttribute(name = "remote-host-check-interval")
    private long remoteHostCheckInterval;


    public RemoteRepository()
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

    public long getTimeout()
    {
        return timeout;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public int getRetries()
    {
        return retries;
    }

    public void setRetries(int retries)
    {
        this.retries = retries;
    }

    public long getRemoteHostCheckInterval()
    {
        return remoteHostCheckInterval;
    }

    public void setRemoteHostCheckInterval(long remoteHostCheckInterval)
    {
        this.remoteHostCheckInterval = remoteHostCheckInterval;
    }

}
