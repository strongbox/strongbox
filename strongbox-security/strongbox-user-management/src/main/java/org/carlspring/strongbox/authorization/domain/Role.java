package org.carlspring.strongbox.authorization.domain;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.authorization.dto.RoleData;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.dto.AccessModelDto;
import org.carlspring.strongbox.users.dto.AccessModelData;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class Role implements RoleData
{

    private final String name;

    private final String description;

    private final AccessModelData accessModel;

    public Role(final RoleDto source)
    {
        this.name = source.getName();
        this.description = source.getDescription();
        this.accessModel = immuteAccessModel(source.getAccessModel());
    }

    private AccessModelData immuteAccessModel(AccessModelDto source)
    {
        return source != null ? new AccessModel(source) : null;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public AccessModelData getAccessModel()
    {
        return accessModel;
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
        return Objects.equals(accessModel, that.accessModel);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
