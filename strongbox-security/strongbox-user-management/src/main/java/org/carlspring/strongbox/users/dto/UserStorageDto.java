package org.carlspring.strongbox.users.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@XmlRootElement(name = "storage")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserStorageDto
{

    @XmlElement(name = "repository")
    @XmlElementWrapper(name = "repositories")
    private Set<UserRepositoryDto> repositories = new LinkedHashSet<>();

    @XmlAttribute(name = "id")
    private String storageId;

    public UserStorageDto()
    {
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof UserStorageDto))
        {
            return false;
        }
        final UserStorageDto that = (UserStorageDto) o;
        return Objects.equals(storageId, that.storageId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(storageId);
    }

    public Set<UserRepositoryDto> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(Set<UserRepositoryDto> repositories)
    {
        this.repositories = repositories;
    }

    public UserRepositoryDto putIfAbsent(String repositoryId,
                                         UserRepositoryDto repository)
    {
        repository.setRepositoryId(repositoryId);
        if (repositories == null)
        {
            repositories = new HashSet<>();
        }
        UserRepositoryDto item = repositories.stream().filter(
                repo -> repositoryId.equals(repo.getRepositoryId())).findFirst().orElse(repository);
        repositories.add(item);
        return item;
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
