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
public class Features
        extends GenericEntity
{

    private Map<String, Collection<String>> perRepositoryAuthorities;

    public Features()
    {
        perRepositoryAuthorities = new HashMap<>();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Features features = (Features) o;
        return Objects.equal(perRepositoryAuthorities, features.perRepositoryAuthorities);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(perRepositoryAuthorities);
    }

    public Map<String, Collection<String>> getPerRepositoryAuthorities()
    {
        return perRepositoryAuthorities;
    }

    public void setPerRepositoryAuthorities(Map<String, Collection<String>> perRepositoryAuthorities)
    {
        this.perRepositoryAuthorities = perRepositoryAuthorities;
    }


    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Features{");
        sb.append("perRepositoryAuthorities=")
          .append(perRepositoryAuthorities);
        sb.append('}');
        return sb.toString();
    }
}
