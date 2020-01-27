package org.carlspring.strongbox.domain;

public interface RemoteArtifact extends Artifact
{

    public Boolean getIsCached();

    public void setIsCached(Boolean value);
    
}
