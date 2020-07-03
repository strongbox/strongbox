package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.DomainEntity;
import org.neo4j.ogm.annotation.NodeEntity;

import static org.carlspring.strongbox.db.schema.Vertices.SECURITY_ROLE;

/**
 * @author ankit.tomar
 */
@NodeEntity(SECURITY_ROLE)
public class SecurityRoleEntity extends DomainEntity implements SecurityRole
{

    public SecurityRoleEntity()
    {
    }

    public SecurityRoleEntity(String role)
    {
        setUuid(role);
    }

}
