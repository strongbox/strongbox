package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.DomainEntity;
import static org.carlspring.strongbox.db.schema.Vertices.USER_ROLE;

/**
 * @author ankit.tomar
 */
@NodeEntity(USER_ROLE)
public class UserRoleEntity extends DomainEntity implements UserRole
{

    public UserRoleEntity()
    {
    }

    public UserRoleEntity(String role)
    {
        setUuid(role);
    }

    @Override
    public String getUserRole()
    {
        return getUuid();
    }

}
