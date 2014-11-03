package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.security.jaas.Credentials;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "remote-repository")
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteRepository
{

    @XmlElement
    private String url;

    @XmlElement(name = "download-remote-indexes")
    private boolean downloadRemoteIndexes;

    @XmlElement(name = "auto-blocking")
    private boolean autoBlocking;

    @XmlElement(name = "checksum-validation")
    private boolean checksumValidation;

    @XmlElement(name = "credentials")
    private Credentials credentials;

    @XmlElement(name = "checksum-policy")
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

    public Credentials getCredentials()
    {
        return credentials;
    }

    public void setCredentials(Credentials credentials)
    {
        this.credentials = credentials;
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
