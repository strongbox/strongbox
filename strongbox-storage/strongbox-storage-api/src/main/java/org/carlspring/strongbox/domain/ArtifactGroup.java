package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.annotation.Nonnull;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
@MappedSuperclass
public abstract class ArtifactGroup
        extends GenericEntity
{

    @OneToMany
    private Set<ArtifactEntry> artifactEntries;

    /**
     * Should be unique per type
     */
    private String name;

    private String type;

    public ArtifactGroup()
    {
        type = getClass().getName();
    }

    @Nonnull
    public Set<ArtifactEntry> getArtifactEntries()
    {
        return artifactEntries != null ? artifactEntries : Collections.emptySet();
    }

    public void setArtifactEntries(Set<ArtifactEntry> artifactEntries)
    {
        this.artifactEntries = artifactEntries;
    }

    public void addArtifactEntry(ArtifactEntry artifactEntry)
    {
        if (artifactEntries == null)
        {
            artifactEntries = new HashSet<>();
        }
        artifactEntries.add(artifactEntry);
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
