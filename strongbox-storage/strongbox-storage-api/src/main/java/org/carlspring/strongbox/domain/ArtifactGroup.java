package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.persistence.ManyToMany;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
public abstract class ArtifactGroup
        extends GenericEntity
{

    @ManyToMany
    private Set<ArtifactEntry> artifactEntries;

    public abstract void setName(String name);

    public abstract String getName();

    public String getType()
    {
        return getClass().getName();
    }
}
