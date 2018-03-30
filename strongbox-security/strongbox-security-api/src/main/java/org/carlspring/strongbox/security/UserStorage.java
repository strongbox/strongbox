package org.carlspring.strongbox.security;

import javax.xml.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@XmlRootElement(name = "storage")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserStorage
{

    @XmlElement(name = "repository")
    @XmlElementWrapper(name = "repositories")
    private Set<UserRepository> repositories = new LinkedHashSet<>();

    @XmlAttribute(name = "id")
    private String storageId;

    public UserStorage()
    {
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserStorage storage = (UserStorage) o;
        return Objects.equal(repositories, storage.repositories) &&
               Objects.equal(storageId, storage.storageId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(repositories, storageId);
    }

    public Set<UserRepository> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(Set<UserRepository> repositories)
    {
        this.repositories = repositories;
    }

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Storage{");
        sb.append("repositories=")
          .append(repositories);
        sb.append(", storageId='")
          .append(storageId)
          .append('\'');
        sb.append('}');
        return sb.toString();
    }
}
