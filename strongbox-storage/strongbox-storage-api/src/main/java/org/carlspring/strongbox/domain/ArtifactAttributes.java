package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

import java.util.Date;

/**
 * @author Przemyslaw Fusik
 */
public class ArtifactAttributes
        extends GenericEntity
{

    private Long sizeInBytes;

    private Date lastUpdated;

    private Date lastUsed;

    public Long getSizeInBytes()
    {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes)
    {
        this.sizeInBytes = sizeInBytes;
    }

    public Date getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }

    public Date getLastUsed()
    {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed)
    {
        this.lastUsed = lastUsed;
    }
}
