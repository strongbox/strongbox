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
@XmlRootElement(name = "repository")
@XmlAccessorType(XmlAccessType.FIELD)
public class Repository
        extends GenericEntity
{

    @XmlElement
    private Privileges privileges;

    @XmlAttribute(name = "id",
                  required = true)
    private String repositoryId;

    public Repository()
    {
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Repository that = (Repository) o;
        return Objects.equal(privileges, that.privileges) &&
               Objects.equal(repositoryId, that.repositoryId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(privileges, repositoryId);
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


    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Repository{");
        sb.append("privileges=")
          .append(privileges);
        sb.append(", repositoryId='")
          .append(repositoryId)
          .append('\'');
        sb.append('}');
        return sb.toString();
    }
}
