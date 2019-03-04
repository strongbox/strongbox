package org.carlspring.strongbox.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.carlspring.strongbox.artifact.ArtifactGroup;
import org.carlspring.strongbox.data.domain.GenericEntity;

/**
 * @author Przemyslaw Fusik
 */
@Entity
@MappedSuperclass
public class ArtifactGroupEntry extends GenericEntity implements ArtifactGroup
{

    private String name;
    @ManyToOne(cascade = { CascadeType.DETACH,
                           CascadeType.MERGE,
                           CascadeType.PERSIST,
                           CascadeType.REFRESH })
    private Set<ArtifactEntry> artifactEntries = new HashSet<>();

    public ArtifactGroupEntry()
    {
        super();
    }

    public ArtifactGroupEntry(String name)
    {
        super();
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Nonnull
    public Set<ArtifactEntry> getArtifactEntries()
    {
        return artifactEntries != null ? artifactEntries : Collections.emptySet();
    }

    public ArtifactEntry putArtifactEntry(ArtifactEntry artifactEntry)
    {
        if (!artifactEntries.contains(artifactEntry))
        {
            artifactEntries.add(artifactEntry);

            return artifactEntry;
        }

        artifactEntries.remove(artifactEntry);
        artifactEntries.add(artifactEntry);

        return artifactEntry;
    }

}
