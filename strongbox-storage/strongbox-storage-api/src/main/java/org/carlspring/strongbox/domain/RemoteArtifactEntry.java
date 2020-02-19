package org.carlspring.strongbox.domain;

import javax.persistence.Entity;

/**
 * @author Sergey Bespalov
 */
@Entity
public class RemoteArtifactEntry
        extends ArtifactEntity
{

    private Boolean isCached = Boolean.FALSE;

    public RemoteArtifactEntry()
    {
        super();
    }

    public Boolean getIsCached()
    {
        return isCached;
    }

    public void setIsCached(Boolean isCached)
    {
        this.isCached = isCached;
    }
}
