package org.carlspring.strongbox.users.dto;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;

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
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "repository")
@XmlAccessorType(XmlAccessType.NONE)
public class UserRepositoryDto
{

    @XmlAttribute(name = "id", required = true)
    private String repositoryId;

    @XmlElement(name = "privilege")
    @XmlElementWrapper(name = "repository-privileges")
    private Set<PrivilegeDto> repositoryPrivileges = new LinkedHashSet<>();

    @XmlElement(name = "path-privilege")
    @XmlElementWrapper(name = "path-privileges")
    private Set<UserPathPrivilegesDto> pathPrivileges = new LinkedHashSet<>();

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(final String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public Set<PrivilegeDto> getRepositoryPrivileges()
    {
        return repositoryPrivileges;
    }

    public Set<UserPathPrivilegesDto> getPathPrivileges()
    {
        return pathPrivileges;
    }

    public Optional<UserPathPrivilegesDto> getPathPrivilege(final String path,
                                                            final boolean wildcard)
    {
        return pathPrivileges.stream()
                             .filter(p -> p.getPath().equals(path) && (p.isWildcard() == wildcard))
                             .findFirst();
    }
}
