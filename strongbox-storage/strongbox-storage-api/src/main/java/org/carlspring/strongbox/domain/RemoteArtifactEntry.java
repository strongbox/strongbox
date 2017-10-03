package org.carlspring.strongbox.domain;

import java.io.Serializable;

/**
 * @author Sergey Bespalov
 *
 */
public class RemoteArtifactEntry
        extends ArtifactEntry
        implements Serializable
{

    private Boolean isCached = Boolean.FALSE;

    public Boolean getIsCached()
    {
        return isCached;
    }

    public void setIsCached(Boolean isCached)
    {
        this.isCached = isCached;
    }

}
