package org.carlspring.strongbox.storage.repository.remote;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class RemoteRepository
{

    private final MutableRemoteRepository delegate;

    public RemoteRepository(final MutableRemoteRepository delegate)
    {
        this.delegate = delegate;
    }

    public String getUrl()
    {
        return delegate.getUrl();
    }

    public boolean isDownloadRemoteIndexes()
    {
        return delegate.isDownloadRemoteIndexes();
    }

    public boolean isAutoBlocking()
    {
        return delegate.isAutoBlocking();
    }

    public boolean isChecksumValidation()
    {
        return delegate.isChecksumValidation();
    }

    public String getUsername()
    {
        return delegate.getUsername();
    }

    public String getPassword()
    {
        return delegate.getPassword();
    }

    public String getChecksumPolicy()
    {
        return delegate.getChecksumPolicy();
    }

    public Integer getCheckIntervalSeconds()
    {
        return delegate.getCheckIntervalSeconds();
    }

    public boolean isAllowsDirectoryBrowsing()
    {
        return delegate.isAllowsDirectoryBrowsing();
    }

    public boolean isAutoImportRemoteSSLCertificate()
    {
        return delegate.isAutoImportRemoteSSLCertificate();
    }
}
