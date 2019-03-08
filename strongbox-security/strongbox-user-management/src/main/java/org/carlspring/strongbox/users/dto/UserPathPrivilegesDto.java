package org.carlspring.strongbox.users.dto;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Alex Oreshkevich
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
public class UserPathPrivilegesDto
        implements Serializable, UserPathPrivelegiesReadContract
{

    private String path;

    private boolean wildcard;

    private Set<PrivilegeDto> privileges = new LinkedHashSet<>();

    public UserPathPrivilegesDto()
    {
    }

    @JsonCreator
    public UserPathPrivilegesDto(@JsonProperty(value = "path", required = true) String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(final String path)
    {
        this.path = path;
    }

    public boolean isWildcard()
    {
        return wildcard;
    }

    public void setWildcard(final boolean wildcard)
    {
        this.wildcard = wildcard;
    }

    public Set<PrivilegeDto> getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(Set<PrivilegeDto> privileges)
    {
        this.privileges = privileges;
    }
}
