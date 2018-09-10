package org.carlspring.strongbox.users.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@XmlRootElement(name = "storage")
@XmlAccessorType(XmlAccessType.NONE)
public class UserStorageDto
{

    @XmlElement(name = "repository")
    @XmlElementWrapper(name = "repositories")
    private Set<UserRepositoryDto> repositories = new LinkedHashSet<>();

    @XmlAttribute(name = "id", required = true)
    private String storageId;

    public Set<UserRepositoryDto> getRepositories()
    {
        return repositories;
    }

    public String getStorageId()
    {
        return storageId;
    }
}
