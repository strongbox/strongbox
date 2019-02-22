package org.carlspring.strongbox.users.security;

import java.util.function.Function;

import org.carlspring.strongbox.authorization.dto.RoleReadContract;
import org.carlspring.strongbox.users.dto.UserAccessModelReadContract;

/**
 * @author sbespalov
 *
 */
public class RuntimeRole implements RoleReadContract
{

    private final RoleReadContract target;
    private final Function<UserAccessModelReadContract, UserAccessModelReadContract> accessModelCustomizer;

    public RuntimeRole(RoleReadContract target,
                       Function<UserAccessModelReadContract, UserAccessModelReadContract> accessModelCustomizer)
    {
        this.target = target;
        this.accessModelCustomizer = accessModelCustomizer;
    }

    public String getName()
    {
        return target.getName();
    }

    public String getDescription()
    {
        return target.getDescription();
    }

    public UserAccessModelReadContract getAccessModel()
    {
        return accessModelCustomizer.apply(target.getAccessModel());
    }

}
