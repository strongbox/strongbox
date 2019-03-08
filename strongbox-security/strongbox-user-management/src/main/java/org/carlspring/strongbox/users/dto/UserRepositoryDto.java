package org.carlspring.strongbox.users.dto;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 * @author Przemyslaw Fusik
 */
public class UserRepositoryDto
        implements UserRepositoryReadContract
{

    private String repositoryId;

    private Set<PrivilegeDto> repositoryPrivileges = new LinkedHashSet<>();

    private Set<UserPathPrivilegesDto> pathPrivileges = new LinkedHashSet<>();

    public UserRepositoryDto()
    {
    }

    @JsonCreator
    public UserRepositoryDto(@JsonProperty(value = "id", required = true) String repositoryId)
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

    public Set<PrivilegeDto> getRepositoryPrivileges()
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
