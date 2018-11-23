package org.carlspring.strongbox.users.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@XmlRootElement(name = "storage")
@XmlAccessorType(XmlAccessType.NONE)
public class UserStorageDto implements UserStorageReadContract
{

    @XmlElement(name = "repository")
    @XmlElementWrapper(name = "repositories")
    private Set<UserRepositoryDto> repositories = new LinkedHashSet<>();

    @XmlAttribute(name = "id", required = true)
    private String storageId;

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserStorageReadContract#getRepositories()
     */
    public Set<UserRepositoryDto> getRepositories()
    {
        return repositories;
    }

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserStorageReadContract#getStorageId()
     */
    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(final String storageId)
    {
        this.storageId = storageId;
    }

    public Optional<UserRepositoryDto> getRepository(final String repositoryId)
    {
        return repositories.stream().filter(r -> r.getRepositoryId().equals(repositoryId)).findFirst();
    }
}
