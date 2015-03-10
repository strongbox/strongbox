package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.security.jaas.Credentials;

import javax.xml.bind.annotation.*;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "remote-repository")
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteRepository
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

}
