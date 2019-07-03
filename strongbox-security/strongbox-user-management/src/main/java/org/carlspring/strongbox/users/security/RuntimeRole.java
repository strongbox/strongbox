package org.carlspring.strongbox.users.security;

import java.util.function.Function;

import javax.annotation.concurrent.Immutable;

import org.carlspring.strongbox.authorization.dto.RoleData;
import org.carlspring.strongbox.users.dto.AccessModelData;

/**
 * @author sbespalov
 */
@Immutable
public class RuntimeRole implements RoleData
{

    private final RoleData target;
    private final Function<AccessModelData, AccessModelData> accessModelCustomizer;

    public RuntimeRole(RoleData target,
                       Function<AccessModelData, AccessModelData> accessModelCustomizer)
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

    public AccessModelData getAccessModel()
    {
        return accessModelCustomizer.apply(target.getAccessModel());
    }

}
