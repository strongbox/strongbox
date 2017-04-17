package org.carlspring.strongbox.users.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Objects;

/**
 * Additional user settings (for example, per-repository access settings).
 *
 * @author Alex Oreshkevich
 */
public class AccessModel
        extends GenericEntity
{

    private Map<String, Collection<String>> perRepositoryAuthorities;

    private Map<String, Collection<String>> perRepositoryPaths;

    public AccessModel()
    {
        perRepositoryAuthorities = new HashMap<>();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessModel that = (AccessModel) o;
        return Objects.equal(perRepositoryAuthorities, that.perRepositoryAuthorities) &&
               Objects.equal(perRepositoryPaths, that.perRepositoryPaths);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(perRepositoryAuthorities, perRepositoryPaths);
    }

    public Map<String, Collection<String>> getPerRepositoryAuthorities()
    {
        return perRepositoryAuthorities;
    }

    public void setPerRepositoryAuthorities(Map<String, Collection<String>> perRepositoryAuthorities)
    {
        this.perRepositoryAuthorities = perRepositoryAuthorities;
    }

    public Map<String, Collection<String>> getPerRepositoryPaths()
    {
        return perRepositoryPaths;
    }

    public void setPerRepositoryPaths(Map<String, Collection<String>> perRepositoryPaths)
    {
        this.perRepositoryPaths = perRepositoryPaths;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("AccessModel{");
        sb.append("perRepositoryAuthorities=")
          .append(perRepositoryAuthorities);
        sb.append(", perRepositoryPaths=")
          .append(perRepositoryPaths);
        sb.append('}');
        return sb.toString();
    }
}
