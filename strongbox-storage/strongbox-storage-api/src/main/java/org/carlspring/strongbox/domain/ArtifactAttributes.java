package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

import java.time.LocalDateTime;

/**
 * @author Przemyslaw Fusik
 */
public class ArtifactAttributes
        extends GenericEntity
{

    private Long sizeInBytes;

    private LocalDateTime lastUpdated;

    private LocalDateTime lastUsed;

    public Long getSizeInBytes()
    {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes)
    {
        this.sizeInBytes = sizeInBytes;
    }

    public LocalDateTime getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }

    public LocalDateTime getLastUsed()
    {
        return lastUsed;
    }

    public void setLastUsed(LocalDateTime lastUsed)
    {
        this.lastUsed = lastUsed;
    }


}
