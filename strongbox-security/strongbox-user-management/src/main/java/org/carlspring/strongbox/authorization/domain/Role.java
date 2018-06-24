package org.carlspring.strongbox.authorization.domain;

import org.carlspring.strongbox.authorization.dto.RoleDto;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class Role
{

    private final String name;

    private final String description;

    private final String repository;

    private final Set<String> privileges;

    public Role(final RoleDto source)
    {
        this.name = source.getName();
        this.description = source.getDescription();
        this.repository = source.getRepository();
        this.privileges = immutePrivileges(source.getPrivileges());
    }

    private Set<String> immutePrivileges(final Set<String> source)
    {
        return source != null ? ImmutableSet.copyOf(source) : Collections.emptySet();
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getRepository()
    {
        return repository;
    }

    public Set<String> getPrivileges()
    {
        return privileges;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Role))
        {
            return false;
        }
        final Role that = (Role) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
