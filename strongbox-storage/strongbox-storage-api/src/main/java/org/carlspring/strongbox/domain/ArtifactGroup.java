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

    @Nonnull
    public Set<ArtifactEntry> getArtifactEntries()
    {
        return artifactEntries != null ? artifactEntries : Collections.emptySet();
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
