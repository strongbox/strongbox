package org.carlspring.strongbox.domain;

import java.util.Set;

import org.carlspring.strongbox.data.domain.DomainObject;

public interface ArtifactGroup extends DomainObject
{
    default String getName()
    {
        return getUuid();
    }

    Set<Artifact> getArtifacts();

}