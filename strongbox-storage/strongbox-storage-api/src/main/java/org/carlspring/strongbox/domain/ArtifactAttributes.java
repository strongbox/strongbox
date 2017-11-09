package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.annotation.Nonnull;
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

    ArtifactAttributes()
    {
        LocalDateTime now = LocalDateTime.now();
        lastUpdated = now;
        lastUpdated = now;
    }

    public Long getSizeInBytes()
    {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes)
    {
        this.sizeInBytes = sizeInBytes;
    }

    @Nonnull
    public LocalDateTime getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }

    @Nonnull
    public LocalDateTime getLastUsed()
    {
        return lastUsed;
    }

    public void setLastUsed(LocalDateTime lastUsed)
    {
        this.lastUsed = lastUsed;
    }


}
