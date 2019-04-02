package org.carlspring.strongbox.users.dto;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
public class UserStorageDto
        implements UserStorageReadContract
{

    private Set<UserRepositoryDto> repositories = new LinkedHashSet<>();

    @JsonProperty(value = "id")
    private String storageId;

    public UserStorageDto()
    {
    }

    @JsonCreator
    public UserStorageDto(@JsonProperty(value = "id", required = true) String storageId)
    {
        this.storageId = storageId;
    }

    public Set<UserRepositoryDto> getRepositories()
    {
        return repositories;
    }

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(final String storageId)
    {
        this.storageId = storageId;
    }

    public Optional<UserRepositoryDto> getRepository(final String repositoryId)
    {
        return repositories.stream().filter(r -> r.getRepositoryId().equals(repositoryId)).findFirst();
    }
}
