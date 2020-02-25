package org.carlspring.strongbox.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.carlspring.strongbox.data.domain.DomainEntity;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Przemyslaw Fusik
 */
@NodeEntity(Vertices.ARTIFACT_GROUP)
public class ArtifactGroupEntity extends DomainEntity implements ArtifactGroup
{

    private String name;
    @Relationship(type = Edges.ARTIFACT_GROUP_HAS_ARTIFACTS, direction = Relationship.OUTGOING)
    private final Set<ArtifactEntity> artifactEntries = new HashSet<>();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public Set<Artifact> getArtifacts()
    {
        return artifactEntries != null ? Collections.unmodifiableSet(artifactEntries) : Collections.emptySet();
    }

    public ArtifactEntity putArtifactEntry(ArtifactEntity artifactEntry)
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
