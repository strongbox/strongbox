package org.carlspring.strongbox.users.dto;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;

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
@XmlRootElement(name = "repository")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserRepositoryDto
{

    @XmlElement(name = "privilege")
    @XmlElementWrapper(name = "privileges")
    private Set<PrivilegeDto> privileges = new LinkedHashSet<>();

    @XmlAttribute(name = "id",
            required = true)
    private String repositoryId;

    @XmlElement(name = "path-permissions")
    private UserPathPermissionsDto pathPermissions;

    public UserRepositoryDto()
    {
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof UserRepositoryDto))
        {
            return false;
        }
        final UserRepositoryDto that = (UserRepositoryDto) o;
        return java.util.Objects.equals(repositoryId, that.repositoryId);
    }

    @Override
    public int hashCode()
    {
        return java.util.Objects.hash(repositoryId);
    }

    public Set<PrivilegeDto> getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(Set<PrivilegeDto> privileges)
    {
        this.privileges = privileges;
    }

    public UserPathPermissionsDto getPathPermissions()
    {
        return pathPermissions;
    }

    public void setPathPermissions(UserPathPermissionsDto pathPermissions)
    {
        this.pathPermissions = pathPermissions;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("UserRepository{");
        sb.append("privileges=")
          .append(privileges);
        sb.append(", repositoryId='")
          .append(repositoryId)
          .append('\'');
        sb.append(", pathPermissions=")
          .append(pathPermissions);
        sb.append('}');
        return sb.toString();
    }

    public UserPathPermissionsDto setIfAbsent(final UserPathPermissionsDto userPathPermissions)
    {
        if (pathPermissions == null)
        {
            setPathPermissions(userPathPermissions);
        }
        return pathPermissions;
    }
}
