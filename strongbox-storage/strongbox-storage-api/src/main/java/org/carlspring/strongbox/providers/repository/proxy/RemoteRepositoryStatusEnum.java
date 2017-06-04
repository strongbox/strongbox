package org.carlspring.strongbox.providers.repository.proxy;

/**
 * @author carlspring
 */
public enum RemoteRepositoryStatusEnum
{

    ONLINE("Online"),

    OFFLINE("Offline"),

    CHECKING("Checking"),

    UNKNOWN("Unknown");

    private String status;


    RemoteRepositoryStatusEnum(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

}
