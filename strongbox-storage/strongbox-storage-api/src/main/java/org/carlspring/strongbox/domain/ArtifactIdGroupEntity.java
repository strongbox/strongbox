package org.carlspring.strongbox.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.carlspring.strongbox.data.domain.DomainEntity;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.reduceHierarchy;

/**
 * @author Przemyslaw Fusik
 * @author sbespalov
 */
@NodeEntity(Vertices.ARTIFACT_ID_GROUP)
public class ArtifactIdGroupEntity extends DomainEntity implements ArtifactIdGroup
{

    private String storageId;
    private String repositoryId;
    private String name;
    @Relationship(type = Edges.ARTIFACT_GROUP_HAS_ARTIFACTS, direction = Relationship.OUTGOING)
    private final Set<Artifact> artifacts = new HashSet<>();

    public ArtifactIdGroupEntity()
    {
    }

    public ArtifactIdGroupEntity(String storageId,
                                 String repositoryId,
                                 String name)
    {
        setUuid(String.format("%s-%s-%s", storageId, repositoryId, name));
        this.storageId = storageId;
        this.repositoryId = repositoryId;
        this.name = name;
    }

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

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
        List<Artifact> result = reduceHierarchy(artifacts.stream()
                                                         .flatMap(a -> a.getHierarchyChild() == null
                                                                 ? Stream.of(a)
                                                                 : Stream.of(a,
                                                                             a.getHierarchyChild()))
                                                         .collect(Collectors.toList()));
        return Collections.unmodifiableSet(new HashSet<>(result));
    }

    @Override
    public void addArtifact(Artifact artifact)
    {
        Artifact artifactParent = Optional.of(artifact).map(a -> a.getHierarchyParent()).orElse(artifact);
        artifacts.remove(artifactParent);
        artifacts.add(artifactParent);
    }

    @Override
    public void removeArtifact(Artifact artifact)
    {
        artifacts.remove(Optional.of(artifact).map(a -> a.getHierarchyParent()).orElse(artifact));
    }
    
}
