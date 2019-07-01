package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.storage.repository.RepositoryDto;

/**
 * @author mtodorov
 */
public enum IndexingRequest
{

    ADD (1),

    DELETE (2);

    private int type;

    private RepositoryDto repository;

    private String artifactPath;


    IndexingRequest(int type)
    {
        this.type = type;
    }

    IndexingRequest(int type,
                    RepositoryDto repository,
                    String artifactPath)
    {
        this.type = type;
        this.repository = repository;
        this.artifactPath = artifactPath;
    }

    public int getType()
    {
        return type;
    }

    public RepositoryDto getRepository()
    {
        return repository;
    }

    public String getArtifactPath()
    {
        return artifactPath;
    }

}
