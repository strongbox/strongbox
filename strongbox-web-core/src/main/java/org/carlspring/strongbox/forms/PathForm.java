package org.carlspring.strongbox.forms;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

public class PathForm
{

    @NotEmpty(message = "A storage id must be specified.")
    @Pattern(regexp = "[a-zA-Z0-9\\-_.]+")
    private String storageId;

    @NotEmpty(message = "An repository id must be specified.")
    @Pattern(regexp = "[a-zA-Z0-9\\-_.]+")
    private String repositoryId;

    @NotEmpty(message = "A path property must be specified")
    private String path;

    private boolean force;

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

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public boolean isForce()
    {
        return force;
    }

    public void setForce(boolean force)
    {
        this.force = force;
    }
}
