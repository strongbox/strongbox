package org.carlspring.strongbox.security;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 */
@XmlRootElement(name = "repositories")
@XmlAccessorType(XmlAccessType.FIELD)
public class Repositories
        extends GenericEntity
{

    @XmlElement(name = "repository")
    private Set<Repository> repositories = new LinkedHashSet<>();


    public Repositories()
    {
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Repositories that = (Repositories) o;
        return Objects.equal(repositories, that.repositories);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(repositories);
    }

    public Set<Repository> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(Set<Repository> repositories)
    {
        this.repositories = repositories;
    }


    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Repositories{");
        sb.append("repositories=")
          .append(repositories);
        sb.append('}');
        return sb.toString();
    }
}
