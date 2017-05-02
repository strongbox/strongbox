package org.carlspring.strongbox.security;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 */
@XmlRootElement(name = "storage")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserStorage
        extends GenericEntity
{

    @XmlElement(name = "repositories")
    private UserRepositories repositories;

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

    public UserRepositories getRepositories()
    {
        return repositories;
    }

    public void setRepositories(UserRepositories repositories)
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
