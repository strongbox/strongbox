package org.carlspring.strongbox.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Transient;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.domain.DomainEntity;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author carlspring
 * @author sbespalov
 */
@NodeEntity(Vertices.ARTIFACT)
public class ArtifactEntity
        extends DomainEntity implements Artifact
{

    private String storageId;

    private String repositoryId;

    @Relationship(type = Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES, direction = Relationship.OUTGOING)
    private GenericArtifactCoordinatesEntity artifactCoordinates;

    @Relationship(type = Edges.ARTIFACT_HAS_TAGS, direction = Relationship.OUTGOING)
    private Set<ArtifactTag> tagSet;

    private Set<String> checksums = new HashSet<>();

    private Set<String> filenames = new LinkedHashSet<>();

    private Long sizeInBytes;

    private Date lastUpdated;

    private Date lastUsed;

    private Date created;

    private Integer downloadCount = Integer.valueOf(0);

    private final ArtifactArchiveListing artifactArchiveListing = new ArtifactEntityArchiveListing();

    public ArtifactEntity()
    {
    }

    public ArtifactEntity(String storageId,
                          String repositoryId,
                          GenericArtifactCoordinatesEntity artifactCoordinates)
    {
        Objects.nonNull(artifactCoordinates);

        this.storageId = storageId;
        this.repositoryId = repositoryId;
        this.artifactCoordinates = artifactCoordinates;

        artifactCoordinates.getLayoutArtifactCoordinates().buildPath();
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
        return artifactCoordinates.getLayoutArtifactCoordinates();
    }

    @Override
    public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        this.artifactCoordinates = ((LayoutArtifactCoordinatesEntity) artifactCoordinates).getGenericArtifactCoordinates();
    }

    @Override
    public Set<ArtifactTag> getTagSet()
    {
        return tagSet = Optional.ofNullable(tagSet).orElse(new HashSet<>());
    }

    protected void setTagSet(Set<ArtifactTag> tagSet)
    {
        this.tagSet = tagSet;
    }

    @Override
    public Map<String, String> getChecksums()
    {
        return checksums.stream().collect(Collectors.toMap(e -> e.substring(1, e.indexOf("}")), e -> e.substring(e.indexOf("}") + 1)));
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
    public Date getLastUpdated()
    {
        return lastUpdated != null ? new Date(lastUpdated.getTime()) : null;
    }

    @Override
    public void setLastUpdated(Date lastUpdated)
    {
        this.lastUpdated = lastUpdated != null ? new Date(lastUpdated.getTime()) : null;
    }

    @Override
    public Date getLastUsed()
    {
        return lastUsed != null ? new Date(lastUsed.getTime()) : null;
    }

    @Override
    public void setLastUsed(Date lastUsed)
    {
        this.lastUsed = lastUsed != null ? new Date(lastUsed.getTime()) : null;
    }

    @Override
    public Date getCreated()
    {
        return created;
    }

    @Override
    public void setCreated(Date created)
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
    public ArtifactArchiveListing getArtifactArchiveListing()
    {
        return artifactArchiveListing;
    }

    @Override
    @Transient
    public String getArtifactPath()
    {
        return Optional.of(getArtifactCoordinates())
                       .map(c -> c.buildPath())
                       .orElseThrow(() -> new IllegalStateException("ArtifactCoordinates required to be set."));
    }

    public class ArtifactEntityArchiveListing implements ArtifactArchiveListing
    {

        public Set<String> getFilenames()
        {
            return ArtifactEntity.this.filenames;
        }

        public void setFilenames(final Set<String> filenames)
        {
            ArtifactEntity.this.filenames = filenames;
        }

    }

}
