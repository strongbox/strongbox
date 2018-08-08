package org.carlspring.strongbox.forms.configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
public class StorageForm
{

    @NotEmpty(message = "An id must be specified.")
    private String id;

    private String basedir;

    @Valid
    private List<RepositoryForm> repositories;

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public String getBasedir()
    {
        return basedir;
    }

    public void setBasedir(final String basedir)
    {
        this.basedir = basedir;
    }

    public List<RepositoryForm> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(final List<RepositoryForm> repositories)
    {
        this.repositories = repositories;
    }
}
