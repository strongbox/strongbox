package org.carlspring.strongbox.security;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 */
@XmlRootElement(name = "repository")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserRepository
{

    @XmlElement
    private Privileges privileges;

    @XmlAttribute(name = "id",
                  required = true)
    private String repositoryId;

    @XmlElement(name = "path-permissions")
    private UserPathPermissions pathPermissions;

    public UserRepository()
    {
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRepository that = (UserRepository) o;
        return Objects.equal(privileges, that.privileges) &&
               Objects.equal(repositoryId, that.repositoryId) &&
               Objects.equal(pathPermissions, that.pathPermissions);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(privileges, repositoryId, pathPermissions);
    }

    public Privileges getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(Privileges privileges)
    {
        this.privileges = privileges;
    }

    public UserPathPermissions getPathPermissions()
    {
        return pathPermissions;
    }

    public void setPathPermissions(UserPathPermissions pathPermissions)
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
}
