package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.domain.DomainObject;
import org.carlspring.strongbox.gremlin.adapters.EntityHierarchyNode;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public interface Artifact extends DomainObject, EntityHierarchyNode<Artifact>
{

    String getStorageId();

    void setStorageId(String storageId);

    String getRepositoryId();

    void setRepositoryId(String repositoryId);

    ArtifactCoordinates getArtifactCoordinates();

    void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates);

    Set<ArtifactTag> getTagSet();

    Map<String, String> getChecksums();

    void setChecksums(Map<String, String> digestMap);

    Long getSizeInBytes();

    void setSizeInBytes(Long sizeInBytes);

    LocalDateTime getLastUpdated();

    void setLastUpdated(LocalDateTime lastUpdated);

    LocalDateTime getLastUsed();

    void setLastUsed(LocalDateTime lastUsed);

    LocalDateTime getCreated();

    void setCreated(LocalDateTime created);

    Integer getDownloadCount();

    void setDownloadCount(Integer downloadCount);

    Set<ArtifactArchiveListing> getArtifactArchiveListings();

    String getArtifactPath();

}