package org.carlspring.strongbox.providers.repository.proxy;

/**
 * @author carlspring
 */
public class RemoteRepositoryStatusInfo
{

    private String url;

    private String status;

    private long lastCheckedStatus;


    public RemoteRepositoryStatusInfo()
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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public long getLastCheckedStatus()
    {
        return lastCheckedStatus;
    }

    public void setLastCheckedStatus(long lastCheckedStatus)
    {
        this.lastCheckedStatus = lastCheckedStatus;
    }

}
