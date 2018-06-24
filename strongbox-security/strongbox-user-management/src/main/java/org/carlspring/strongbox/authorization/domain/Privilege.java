package org.carlspring.strongbox.authorization.domain;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class Privilege
{

    private final String name;

    private final String description;

    public Privilege(final PrivilegeDto source)
    {
        this.name = source.getName();
        this.description = source.getDescription();
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }
}
