package org.carlspring.strongbox.security;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 */
@XmlRootElement(name = "repository")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserRepository
        extends GenericEntity
{

    @XmlElement
    private Privileges privileges;

    @XmlAttribute(name = "id",
                  required = true)
    private String repositoryId;

    @XmlElement(name = "path")
    @XmlElementWrapper(name = "granted-paths")
    private Set<String> grantedPaths = new HashSet<>();

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
               Objects.equal(grantedPaths, that.grantedPaths);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(privileges, repositoryId, grantedPaths);
    }

    public Privileges getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(Privileges privileges)
    {
        this.privileges = privileges;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public Set<String> getGrantedPaths()
    {
        return grantedPaths;
    }

    public void setGrantedPaths(Set<String> grantedPaths)
    {
        this.grantedPaths = grantedPaths;
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
        sb.append(", grantedPaths=")
          .append(grantedPaths);
        sb.append('}');
        return sb.toString();
    }
}
