package org.carlspring.strongbox.domain;

import java.util.Set;

import org.carlspring.strongbox.data.domain.DomainObject;

public interface ArtifactGroup extends DomainObject
{
    String getName();

    Set<Artifact> getArtifacts();
    
    void addArtifact(Artifact artifact);
    
    void removeArtifact(Artifact artifact);

}