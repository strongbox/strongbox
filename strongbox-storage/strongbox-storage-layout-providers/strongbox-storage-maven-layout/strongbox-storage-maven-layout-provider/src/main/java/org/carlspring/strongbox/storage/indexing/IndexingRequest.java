package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.storage.repository.MutableRepository;

/**
 * @author mtodorov
 */
public enum IndexingRequest
{

    ADD (1),
    DELETE (2);

    private int type;

    private MutableRepository repository;

    private String artifactPath;


    IndexingRequest(int type)
    {
        this.type = type;
    }

    IndexingRequest(int type,
                    MutableRepository repository,
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

    public void setType(int type)
    {
        this.type = type;
    }

    public MutableRepository getRepository()
    {
        return repository;
    }

    public void setRepository(MutableRepository repository)
    {
        this.repository = repository;
    }

    public String getArtifactPath()
    {
        return artifactPath;
    }

    public void setArtifactPath(String artifactPath)
    {
        this.artifactPath = artifactPath;
    }

}
