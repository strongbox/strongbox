package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.DomainObject;

/**
 * @author sbespalov
 */
public interface RepositoryArtifactIdGroup<T extends Artifact> extends ArtifactGroup<T>, DomainObject
{
    String getStorageId();

    String getRepositoryId();

    String getArtifactId();
}