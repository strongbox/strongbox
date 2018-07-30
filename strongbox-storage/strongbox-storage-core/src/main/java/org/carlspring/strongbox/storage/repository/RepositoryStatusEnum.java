package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.api.Describable;

/**
 * @author mtodorov
 */
public enum RepositoryStatusEnum implements Describable
{

    IN_SERVICE("In Service"),

    OUT_OF_SERVICE("Out of Service");

    private String status;


    RepositoryStatusEnum(String status)
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

    @Override
    public String describe()
    {
        return getStatus();
    }
}
