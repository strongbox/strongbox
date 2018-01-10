package org.carlspring.strongbox.providers.repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.carlspring.strongbox.artifact.ArtifactTag;

public class RepositorySearchRequest
{
    private String storageId;
    private String repositoryId;
    private Map<String, String> coordinates = new HashMap<>();
    private Set<ArtifactTag> tagSet = new HashSet<>();
    private boolean strict;

    public RepositorySearchRequest(String storageId,
                                   String repositoryId)
    {
        super();
        this.storageId = storageId;
        this.repositoryId = repositoryId;
    }

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public Map<String, String> getCoordinates()
    {
        return coordinates;
    }

    public void setCoordinates(Map<String, String> coordinates)
    {
        this.coordinates = coordinates;
    }
    
    public Set<ArtifactTag> getTagSet()
    {
        return tagSet;
    }

    public void setTagSet(Set<ArtifactTag> tagSet)
    {
        this.tagSet = tagSet;
    }

    public boolean isStrict()
    {
        return strict;
    }

    public void setStrict(boolean strict)
    {
        this.strict = strict;
    }

}
