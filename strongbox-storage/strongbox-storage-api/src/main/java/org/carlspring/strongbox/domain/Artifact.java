package org.carlspring.strongbox.domain;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.data.domain.DomainObject;

public interface Artifact extends DomainObject
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

    Date getLastUpdated();

    void setLastUpdated(Date lastUpdated);

    Date getLastUsed();

    void setLastUsed(Date lastUsed);

    Date getCreated();

    void setCreated(Date created);

    Integer getDownloadCount();

    void setDownloadCount(Integer downloadCount);

    ArtifactArchiveListing getArtifactArchiveListing();

    String getArtifactPath();

}