package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.artifact.ArtifactGroup;
import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    private Set<ArtifactEntry> artifactEntries;

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

    public ArtifactEntry replaceArtifactEntry(ArtifactEntry artifactEntry) {
        if (!artifactEntries.contains(artifactEntry)) {
            return artifactEntry;
        }
        
        artifactEntries.remove(artifactEntry);
        artifactEntries.add(artifactEntry);
        
        return artifactEntry;
    }
    
    public void addArtifactEntry(ArtifactEntry artifactEntry)
    {
        if (artifactEntries == null)
        {
            artifactEntries = new HashSet<>();
        }
        artifactEntries.add(artifactEntry);
    }
}
