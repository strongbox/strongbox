package org.carlspring.strongbox.users.dto;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.carlspring.strongbox.users.domain.Privileges;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 * @author Przemyslaw Fusik
 */
public class UserRepositoryDto
        implements Serializable, UserRepositoryReadContract
{
    @JsonProperty("repositoryId")
    private String repositoryId;

    private Set<Privileges> repositoryPrivileges = new LinkedHashSet<>();

    private Set<UserPathPrivilegesDto> pathPrivileges = new LinkedHashSet<>();

    public UserRepositoryDto()
    {
    }

    @JsonCreator
    public UserRepositoryDto(@JsonProperty(value = "repositoryId", required = true) String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(final String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public Set<Privileges> getRepositoryPrivileges()
    {
        return repositoryPrivileges;
    }

    public Set<UserPathPrivilegesDto> getPathPrivileges()
    {
        return pathPrivileges;
    }

    public Optional<UserPathPrivilegesDto> getPathPrivilege(final String path,
                                                            final boolean wildcard)
    {
        return pathPrivileges.stream()
                             .filter(p -> p.getPath().equals(path) && (p.isWildcard() == wildcard))
                             .findFirst();
    }
}
