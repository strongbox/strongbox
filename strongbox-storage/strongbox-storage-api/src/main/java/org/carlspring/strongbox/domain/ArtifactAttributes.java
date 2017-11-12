package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;
import org.carlspring.strongbox.data.domain.OLocalDateTime;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

/**
 * @author Przemyslaw Fusik
 */
public class ArtifactAttributes
        extends GenericEntity
{

    private Long sizeInBytes;

    private OLocalDateTime lastUpdated;

    private OLocalDateTime lastUsed;

    ArtifactAttributes()
    {
        LocalDateTime now = LocalDateTime.now();
        lastUpdated = new OLocalDateTime(now);
        lastUpdated = new OLocalDateTime(now);
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
        return lastUpdated != null ? lastUpdated.getSubject() : null;
    }

    public void setLastUpdated(LocalDateTime lastUpdated)
    {
        this.lastUpdated = new OLocalDateTime(lastUpdated);
    }

    @Nonnull
    public LocalDateTime getLastUsed()
    {
        return lastUsed != null ? lastUsed.getSubject() : null;
    }

    public void setLastUsed(LocalDateTime lastUsed)
    {
        this.lastUsed = new OLocalDateTime(lastUsed);
    }


}
