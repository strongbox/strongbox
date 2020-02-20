package org.carlspring.strongbox.domain;

import java.util.Set;

import org.carlspring.strongbox.data.domain.DomainObject;

public interface ArtifactGroup<T extends Artifact> extends DomainObject
{
    String getName();

    void setName(String name);
    
    Set<T> getArtifacts();

}