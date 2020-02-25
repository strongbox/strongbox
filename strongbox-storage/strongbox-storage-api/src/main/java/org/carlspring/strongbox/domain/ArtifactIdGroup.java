package org.carlspring.strongbox.domain;

/**
 * @author sbespalov
 */
public interface ArtifactIdGroup extends ArtifactGroup
{
    String getStorageId();

    String getRepositoryId();

    default String getArtifactId() {
        return getName();
    }
}