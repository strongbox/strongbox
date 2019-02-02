package org.carlspring.strongbox.storage;

import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.xml.RepositoryMapAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "storage")
@XmlAccessorType(XmlAccessType.FIELD)
public class MutableStorage
        implements Serializable
{

    @XmlAttribute(required = true)
    private String id;
    
    @XmlAttribute
    private String basedir;

    @Value("${strongbox.storage.booter.basedir:strongbox-vault/storages}")
    private String storageBooterBasedir;

    @Value("${strongbox.vault:strongbox-vault}")
    private String vaultDirectory;

    @XmlElement(name = "repositories")
    @XmlJavaTypeAdapter(RepositoryMapAdapter.class)
    private Map<String, MutableRepository> repositories = new LinkedHashMap<>();


    public MutableStorage()
    {
    }

    public MutableStorage(String id)
    {
        this.id = id;
    }

    public MutableStorage(String id,
                          String basedir)
    {
        this.id = id;
        this.basedir = basedir;
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
            Path basedir;
            if (System.getProperty("strongbox.storage.booter.basedir") != null)
            {
                basedir = Paths.get(System.getProperty("strongbox.storage.booter.basedir"));
            }
            else
            {
                // Assuming this invocation is related to tests:
                basedir = Paths.get(vaultDirectory).resolve("storages");
            }
            
            return basedir.resolve(id).toString();
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
        return !repositories.isEmpty();
    }

    public boolean existsOnFileSystem()
    {
        return Files.exists(Paths.get(getBasedir()));
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
        //    sb.append(", \n\t\tdetachAll='").append(detachAll).append('\'');
        //    sb.append(", \n\t\tversion=").append(version);
        sb.append('}');
        return sb.toString();
    }
    
}
