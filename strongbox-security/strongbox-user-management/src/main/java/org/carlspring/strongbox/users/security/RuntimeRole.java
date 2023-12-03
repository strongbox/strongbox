package org.carlspring.strongbox.users.security;

import java.util.function.Function;

import javax.annotation.concurrent.Immutable;

import org.carlspring.strongbox.authorization.dto.Role;
import org.carlspring.strongbox.users.dto.AccessModel;

/**
 * @author sbespalov
 */
@Immutable
public class RuntimeRole implements Role
{

    private final Role target;
    private final Function<AccessModel, AccessModel> accessModelCustomizer;

    public RuntimeRole(Role target,
                       Function<AccessModel, AccessModel> accessModelCustomizer)
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

    public AccessModel getAccessModel()
    {
        return accessModelCustomizer.apply(target.getAccessModel());
    }

}
