package org.carlspring.strongbox.storage;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.xml.RepositoryMapAdapter;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mtodorov
 */
@Entity
@XmlRootElement(name = "storage")
@XmlAccessorType(XmlAccessType.FIELD)
public class Storage
        implements Serializable
{

    @XmlAttribute(required = true)
    private String id;
    
    @XmlAttribute
    private String basedir;
    
    @XmlElement(name = "repositories")
    @XmlJavaTypeAdapter(RepositoryMapAdapter.class)
    private Map<String, Repository> repositories = new LinkedHashMap<>();


    public Storage()
    {
    }

    public Storage(String id)
    {
        this.id = id;
    }

    public Storage(String id,
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
            if (System.getProperty("strongbox.storage.booter.basedir") != null)
            {
                return System.getProperty("strongbox.storage.booter.basedir") + File.separatorChar + id;
            }
            else
            {
                // Assuming this invocation is related to tests:
                return ConfigurationResourceResolver.getVaultDirectory() + "/storages/" + id;
            }
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

    public Map<String, Repository> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(Map<String, Repository> repositories)
    {
        this.repositories = repositories;
    }

    public void addRepository(Repository repository)
    {
        repositories.put(repository.getId(), repository);
    }

    public Repository getRepository(String repositoryId)
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
        String storagesBasedir;
        if (basedir != null)
        {
            storagesBasedir = basedir;
        }
        else if (System.getProperty("strongbox.storage.booter.storages.basedir") != null)
        {
            storagesBasedir = System.getProperty("strongbox.storage.booter.storages.basedir");
        }
        else
        {
            storagesBasedir = "target/storages";
        }

        File storageDirectory = new File(storagesBasedir, id);

        return storageDirectory.exists();
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
