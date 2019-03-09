package org.carlspring.strongbox.storage;

import org.carlspring.strongbox.storage.repository.MutableRepository;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
@JsonRootName("storage")
public class MutableStorage
        implements Serializable
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
        if (basedir != null)
        {
            return basedir;
        }
        else if (id != null)
        {
            initDefaultBasedir(id);
            return basedir;
        }
        else
        {
            return null;
        }
    }

    public void setBasedir(String basedir)
    {
        this.basedir = basedir;
    }

    public void initDefaultBasedir(String id)
    {
        //TODO: we should rework this to use SpringBoot environment instead of `System.getProperty`
        String storagesBaseDir = System.getProperty("strongbox.storage.booter.basedir");
        Assert.notNull(storagesBaseDir, "System property `strongbox.storage.booter.basedir` should be configured.");
        
        Path basedirPath = Paths.get(storagesBaseDir);
        basedir = basedirPath.resolve(id).toString();
    }

    public Map<String, MutableRepository> getRepositories()
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

    public boolean existsOnFileSystem()
    {
        return Paths.get(getBasedir()).toFile().exists();
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
