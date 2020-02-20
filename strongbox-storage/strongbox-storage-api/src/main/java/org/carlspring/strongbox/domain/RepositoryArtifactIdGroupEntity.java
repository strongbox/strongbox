package org.carlspring.strongbox.domain;

import java.util.Set;

import javax.persistence.Entity;

import org.carlspring.strongbox.data.domain.DomainEntity;
import org.carlspring.strongbox.db.schema.Edges;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Przemyslaw Fusik
 */
@Entity
public class RepositoryArtifactIdGroupEntity
        extends DomainEntity implements RepositoryArtifactIdGroup<ArtifactEntity>
{

    private String storageId;
    private String repositoryId;
    @Relationship(type = Edges.REPOSITORY_ARTIFACT_ID_GROUP_INHERIT_ARTIFACT_GROUP, direction = Relationship.OUTGOING)
    private final ArtifactGroupEntity artifactGroup;

    public RepositoryArtifactIdGroupEntity()
    {
        this(new ArtifactGroupEntity());
    }

    public RepositoryArtifactIdGroupEntity(ArtifactGroupEntity artifactGroup)
    {
        this.artifactGroup = artifactGroup;
    }

    public void setUuid(String uuid)
    {
        super.setUuid(uuid);
        artifactGroup.setUuid(uuid);
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public String getArtifactId()
    {
        return getName();
    }

    public boolean equals(Object obj)
    {
        return artifactGroup.equals(obj);
    }

    public String getName()
    {
        return artifactGroup.getName();
    }

    public void setName(String name)
    {
        artifactGroup.setName(name);
    }

    public Set<ArtifactEntity> getArtifacts()
    {
        return artifactGroup.getArtifacts();
    }

    public ArtifactEntity putArtifactEntry(ArtifactEntity artifactEntry)
    {
        return artifactGroup.putArtifactEntry(artifactEntry);
    }

}
