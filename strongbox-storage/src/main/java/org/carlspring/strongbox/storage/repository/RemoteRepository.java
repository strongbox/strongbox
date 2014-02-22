package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.security.jaas.Credentials;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author mtodorov
 */
public class RemoteRepository
{

    @XStreamAlias(value = "url")
    private String url;

    @XStreamAlias(value = "downloadRemoteIndexes")
    private boolean downloadRemoteIndexes;

    @XStreamAlias(value = "autoBlocking")
    private boolean autoBlocking;

    @XStreamAlias(value = "checksumValidation")
    private boolean checksumValidation;

    @XStreamAlias(value = "credentials")
    private Credentials credentials;

    @XStreamAlias(value = "checksumPolicy")
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
