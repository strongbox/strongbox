package org.carlspring.strongbox.storage;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
@JsonRootName("storage")
public class MutableStorage
        implements Serializable, Storage
{
    private String id;
    
    private String basedir;

    private Map<String, MutableRepository> repositories = new LinkedHashMap<>();

    public MutableStorage()
    {
    }

    @JsonCreator
    public MutableStorage(@JsonProperty(value = "id", required = true) String id)
    {
        this.id = id;
    }

    public boolean containsRepository(String repository)
    {
        return getRepositories().containsKey(repository);
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getBasedir()
    {
        return basedir;
    }

    public void setBasedir(String basedir)
    {
        this.basedir = basedir;
    }

    @Override
    public Map<String, ? extends Repository> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(Map<String, MutableRepository> repositories)
    {
        this.repositories = repositories;
    }

    public void addRepository(MutableRepository repository)
    {
        repositories.put(repository.getId(), repository);
    }

    public MutableRepository getRepository(String repositoryId)
    {
        return repositories.get(repositoryId);
    }

    public void removeRepository(String repositoryId)
    {
        repositories.remove(repositoryId);
    }

    public boolean hasRepositories()
    {
        return !CollectionUtils.isEmpty(repositories);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Storage{");
        sb.append("\n\t\tid='")
          .append(id)
          .append('\'');
        sb.append(", \n\t\tbasedir='").append(basedir).append('\'');
        sb.append(", \n\t\trepositories=").append(repositories);
        sb.append('}');
        return sb.toString();
    }
    
}
