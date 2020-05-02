package org.carlspring.strongbox.domain;

import static org.carlspring.strongbox.db.schema.Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES;
import static org.carlspring.strongbox.db.schema.Edges.ARTIFACT_HAS_TAGS;
import static org.carlspring.strongbox.db.schema.Edges.REMOTE_ARTIFACT_INHERIT_ARTIFACT;
import static org.carlspring.strongbox.db.schema.Vertices.ARTIFACT;
import static org.neo4j.ogm.annotation.Relationship.INCOMING;
import static org.neo4j.ogm.annotation.Relationship.OUTGOING;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.GenericArtifactCoordinates;
import org.carlspring.strongbox.data.domain.DomainEntity;
import org.carlspring.strongbox.gremlin.adapters.DateConverter;

import javax.persistence.Transient;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;

/**
 * @author carlspring
 * @author sbespalov
 */
@NodeEntity(ARTIFACT)
public class ArtifactEntity
        extends DomainEntity implements Artifact
{

    private String storageId;

    private String repositoryId;

    @Relationship(type = ARTIFACT_HAS_ARTIFACT_COORDINATES, direction = OUTGOING)
    private GenericArtifactCoordinates artifactCoordinates;

    @Relationship(type = ARTIFACT_HAS_TAGS, direction = OUTGOING)
    private Set<ArtifactTag> tagSet;

    private Set<String> checksums = new HashSet<>();

    private Long sizeInBytes;

    @Convert(DateConverter.class)
    private LocalDateTime lastUpdated;

    @Convert(DateConverter.class)
    private LocalDateTime lastUsed;

    @Convert(DateConverter.class)
    private LocalDateTime created;

    private Integer downloadCount = Integer.valueOf(0);

    @Relationship(type = REMOTE_ARTIFACT_INHERIT_ARTIFACT, direction = INCOMING)
    private Artifact artifactHierarchyChild;

    ArtifactEntity()
    {
    }

    public ArtifactEntity(String storageId,
                          String repositoryId,
                          ArtifactCoordinates artifactCoordinates)
    {
        Objects.nonNull(artifactCoordinates);

        this.storageId = storageId;
        this.repositoryId = repositoryId;
        this.artifactCoordinates = artifactCoordinates;
        if (getArtifactCoordinates() != null)
        {
            setUuid(String.format("%s-%s-%s", getStorageId(), getRepositoryId(), getArtifactCoordinates().buildPath()));
        }
    }

    @Override
    public String getStorageId()
    {
        return storageId;
    }

    @Override
    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    @Override
    public String getRepositoryId()
    {
        return repositoryId;
    }

    @Override
    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    @Override
    public ArtifactCoordinates getArtifactCoordinates()
    {
        if (artifactCoordinates instanceof ArtifactCoordinates)
        {
            return (ArtifactCoordinates) artifactCoordinates;
        }
        return (ArtifactCoordinates) artifactCoordinates.getHierarchyChild();
    }

    @Override
    public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        this.artifactCoordinates = artifactCoordinates;
    }

    @Override
    public Set<ArtifactTag> getTagSet()
    {
        return tagSet = Optional.ofNullable(tagSet).orElse(new HashSet<>());
    }

    public void setTagSet(Set<ArtifactTag> tagSet)
    {
        this.tagSet = tagSet;
    }

    @Override
    public Map<String, String> getChecksums()
    {
        return checksums.stream()
                        .filter(e -> !e.trim().isEmpty())
                        .collect(Collectors.toMap(e -> e.substring(1, e.indexOf("}")),
                                                  e -> e.substring(e.indexOf("}") + 1)));
    }

    public void setChecksums(Map<String, String> checksums)
    {
        this.checksums.clear();
        this.checksums.addAll(checksums.entrySet()
                                       .stream()
                                       .map(e -> "{" + e.getKey() + "}" + e.getValue())
                                       .collect(Collectors.toSet()));
    }

    public void addChecksums(Set<String> checksums)
    {
        if (checksums == null)
        {
            return;
        }
        checksums.stream()
                 .filter(e -> e.startsWith("{"))
                 .filter(e -> e.indexOf("}") > 1)
                 .filter(e -> !e.endsWith("}"))
                 .forEach(this.checksums::add);
    }

    @Override
    public Long getSizeInBytes()
    {
        return sizeInBytes;
    }

    @Override
    public void setSizeInBytes(Long sizeInBytes)
    {
        this.sizeInBytes = sizeInBytes;
    }

    @Override
    public LocalDateTime getLastUpdated()
    {
        return lastUpdated;
    }

    @Override
    public void setLastUpdated(LocalDateTime lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public LocalDateTime getLastUsed()
    {
        return lastUsed;
    }

    @Override
    public void setLastUsed(LocalDateTime lastUsed)
    {
        this.lastUsed = lastUsed;
    }

    @Override
    public LocalDateTime getCreated()
    {
        return created;
    }

    @Override
    public void setCreated(LocalDateTime created)
    {
        this.created = created;
    }

    @Override
    public Integer getDownloadCount()
    {
        return downloadCount;
    }

    @Override
    public void setDownloadCount(Integer downloadCount)
    {
        this.downloadCount = downloadCount;
    }

    @Override
    @Transient
    public String getArtifactPath()
    {
        return Optional.of(getArtifactCoordinates())
                       .map(c -> c.buildPath())
                       .orElseThrow(() -> new IllegalStateException("ArtifactCoordinates required to be set."));
    }

    public void setHierarchyChild(Artifact artifactHierarchyChild)
    {
        this.artifactHierarchyChild = artifactHierarchyChild;
    }

    @Override
    public Artifact getHierarchyChild()
    {
        return artifactHierarchyChild;
    }

    @Override
    public Artifact getHierarchyParent()
    {
        return null;
    }

}
