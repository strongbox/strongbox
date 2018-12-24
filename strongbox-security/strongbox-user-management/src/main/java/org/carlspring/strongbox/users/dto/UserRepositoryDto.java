package org.carlspring.strongbox.users.dto;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "repository")
@XmlAccessorType(XmlAccessType.NONE)
public class UserRepositoryDto implements UserRepositoryReadContract
{

    @XmlAttribute(name = "id", required = true)
    private String repositoryId;

    @XmlElement(name = "privilege")
    @XmlElementWrapper(name = "repository-privileges")
    private Set<PrivilegeDto> repositoryPrivileges = new LinkedHashSet<>();

    @XmlElement(name = "path-privilege")
    @XmlElementWrapper(name = "path-privileges")
    private Set<UserPathPrivilegesDto> pathPrivileges = new LinkedHashSet<>();

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserRepositoryReadContract#getRepositoryId()
     */
    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(final String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserRepositoryReadContract#getRepositoryPrivileges()
     */
    public Set<PrivilegeDto> getRepositoryPrivileges()
    {
        return repositoryPrivileges;
    }

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserRepositoryReadContract#getPathPrivileges()
     */
    public Set<UserPathPrivilegesDto> getPathPrivileges()
    {
        return pathPrivileges;
    }

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserRepositoryReadContract#getPathPrivilege(java.lang.String, boolean)
     */
    public Optional<UserPathPrivilegesDto> getPathPrivilege(final String path,
                                                            final boolean wildcard)
    {
        return pathPrivileges.stream()
                             .filter(p -> p.getPath().equals(path) && (p.isWildcard() == wildcard))
                             .findFirst();
    }
}
