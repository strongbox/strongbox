package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
@MappedSuperclass
public abstract class ArtifactGroup
        extends GenericEntity
{

    public ArtifactGroup()
    {
        type = getClass().getName();
    }

    @OneToMany
    private Set<ArtifactEntry> artifactEntries;

    /**
     * Should be unique per type
     */
    private String name;

    private String type;

    public Set<ArtifactEntry> getArtifactEntries()
    {
        return artifactEntries;
    }

    public void setArtifactEntries(Set<ArtifactEntry> artifactEntries)
    {
        this.artifactEntries = artifactEntries;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
