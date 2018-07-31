package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.storage.MutableStorage;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.util.CollectionUtils;

/**
 * @author Przemyslaw Fusik
 */
public class StorageOutput
{

    @JsonView(Views.ShortStorage.class)
    private String id;

    @JsonView(Views.ShortStorage.class)
    private String basedir;

    @JsonView(Views.LongStorage.class)
    private List<RepositoryOutput> repositories;

    public StorageOutput()
    {
    }

    public StorageOutput(final MutableStorage storage)
    {
        this.id = storage.getId();
        this.basedir = storage.getBasedir();
        if (!CollectionUtils.isEmpty(storage.getRepositories()))
        {
            this.repositories = storage.getRepositories().values().stream().map(RepositoryOutput::new).collect(
                    Collectors.toList());
        }
    }

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

    public List<RepositoryOutput> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(final List<RepositoryOutput> repositories)
    {
        this.repositories = repositories;
    }
}
