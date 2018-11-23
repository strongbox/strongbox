package org.carlspring.strongbox.users.domain;

import javax.annotation.concurrent.Immutable;

import org.carlspring.strongbox.authorization.dto.PrivelegieReadContract;
import org.carlspring.strongbox.authorization.dto.PrivilegeDto;

@Immutable
public class AccessModelPrivelege implements PrivelegieReadContract
{

    private final String name;
    private final String description;

    public AccessModelPrivelege(PrivilegeDto dto)
    {
        super();
        this.name = dto.getName();
        this.description = dto.getDescription();
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

}
