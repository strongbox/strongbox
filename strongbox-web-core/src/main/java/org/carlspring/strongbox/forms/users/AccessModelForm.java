package org.carlspring.strongbox.forms.users;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserPathPermissionDto;
import org.carlspring.strongbox.users.dto.UserPathPermissionsDto;
import org.carlspring.strongbox.users.dto.UserRepositoryDto;
import org.carlspring.strongbox.users.dto.UserStorageDto;
import org.carlspring.strongbox.validation.users.ValidAccessModelMapKey;
import org.carlspring.strongbox.validation.users.ValidAccessModelMapValue;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import static org.carlspring.strongbox.users.domain.Permissions.READ;
import static org.carlspring.strongbox.users.domain.Permissions.READ_WRITE;

public class AccessModelForm
        implements Serializable
{

    @ValidAccessModelMapKey(message = "The repository privileges map keys must follow the pattern '/storages/{storageId}/{repositoryId}'.")
    @ValidAccessModelMapValue(message = "The repository privileges map values must be specified.")
    private Map<String, Collection<String>> repositoryPrivileges;

    @ValidAccessModelMapKey(message = "The URL to privileges map keys must follow the pattern '/storages/{storageId}/{repositoryId}'.")
    @ValidAccessModelMapValue(message = "The URL to privileges map values must be specified.")
    private Map<String, Collection<String>> urlToPrivilegesMap;

    @ValidAccessModelMapKey(message = "The wildcard privileges map keys must follow the pattern '/storages/{storageId}/{repositoryId}'.")
    @ValidAccessModelMapValue(message = "The wildcard privileges map values must be specified.")
    private Map<String, Collection<String>> wildCardPrivilegesMap;

    public AccessModelForm()
    {
        repositoryPrivileges = new HashMap<>();
        urlToPrivilegesMap = new HashMap<>();
        wildCardPrivilegesMap = new HashMap<>();
    }

    public Map<String, Collection<String>> getRepositoryPrivileges()
    {
        return repositoryPrivileges;
    }

    public void setRepositoryPrivileges(Map<String, Collection<String>> repositoryPrivileges)
    {
        this.repositoryPrivileges = repositoryPrivileges;
    }

    public Map<String, Collection<String>> getUrlToPrivilegesMap()
    {
        return urlToPrivilegesMap;
    }

    public void setUrlToPrivilegesMap(Map<String, Collection<String>> urlToPrivilegesMap)
    {
        this.urlToPrivilegesMap = urlToPrivilegesMap;
    }

    public Map<String, Collection<String>> getWildCardPrivilegesMap()
    {
        return wildCardPrivilegesMap;
    }

    public void setWildCardPrivilegesMap(Map<String, Collection<String>> wildCardPrivilegesMap)
    {
        this.wildCardPrivilegesMap = wildCardPrivilegesMap;
    }

    public UserAccessModelDto toDto()
    {
        UserAccessModelDto userAccessModel = new UserAccessModelDto();

        // repositoryPrivileges
        if (getRepositoryPrivileges() != null)
        {
            for (Map.Entry<String, Collection<String>> repositoryPrivileges : getRepositoryPrivileges().entrySet())
            {
                String storageIdAndRepositoryId = StringUtils.substringAfter(repositoryPrivileges.getKey(),
                                                                             "/storages/");
                String storageId = StringUtils.substringBefore(storageIdAndRepositoryId, "/");
                String repositoryId = StringUtils.substringAfter(storageIdAndRepositoryId, "/");

                UserStorageDto userStorage = userAccessModel.putIfAbsent(storageId, new UserStorageDto());

                UserRepositoryDto userRepository = userStorage.putIfAbsent(repositoryId, new UserRepositoryDto());

                userRepository.setPrivileges(
                        repositoryPrivileges.getValue().stream().map(name -> new PrivilegeDto(name, null)).collect(
                                Collectors.toSet()));
            }
        }

        // urlToPrivilegesMap
        if (getUrlToPrivilegesMap() != null)
        {
            for (Map.Entry<String, Collection<String>> urlToPrivileges : getUrlToPrivilegesMap().entrySet())
            {
                String storageIdRepositoryIdAndPath = StringUtils.substringAfter(urlToPrivileges.getKey(),
                                                                                 "/storages/");
                String storageId = StringUtils.substringBefore(storageIdRepositoryIdAndPath, "/");
                String repositoryId = StringUtils.substringBetween(storageIdRepositoryIdAndPath, "/", "/");
                String path = StringUtils.substringAfter(storageIdRepositoryIdAndPath,
                                                         storageId + "/" + repositoryId + "/");

                UserStorageDto userStorage = userAccessModel.putIfAbsent(storageId, new UserStorageDto());
                userStorage.setStorageId(storageId);

                UserRepositoryDto userRepository = userStorage.putIfAbsent(repositoryId, new UserRepositoryDto());
                userRepository.setRepositoryId(repositoryId);

                UserPathPermissionsDto userPathPermissions = userRepository.setIfAbsent(new UserPathPermissionsDto());
                UserPathPermissionDto userPathPermission = userPathPermissions.putIfAbsent(path,
                                                                                           new UserPathPermissionDto());

                userPathPermission.setPath(path);
                userPathPermission.setPermission(pathPrivilegesToPermission(urlToPrivileges.getValue()));
            }
        }

        // wildCardPrivilegesMap
        if (getWildCardPrivilegesMap() != null)
        {
            for (Map.Entry<String, Collection<String>> wildcardPrivilegesMap : getWildCardPrivilegesMap().entrySet())
            {
                String storageIdRepositoryIdAndPath = StringUtils.substringAfter(wildcardPrivilegesMap.getKey(),
                                                                                 "/storages/");
                String storageId = StringUtils.substringBefore(storageIdRepositoryIdAndPath, "/");
                String repositoryId = StringUtils.substringBetween(storageIdRepositoryIdAndPath, "/", "/");
                String path = StringUtils.substringAfter(storageIdRepositoryIdAndPath,
                                                         storageId + "/" + repositoryId + "/");

                UserStorageDto userStorage = userAccessModel.putIfAbsent(storageId, new UserStorageDto());
                userStorage.setStorageId(storageId);

                UserRepositoryDto userRepository = userStorage.putIfAbsent(repositoryId, new UserRepositoryDto());
                userRepository.setRepositoryId(repositoryId);

                UserPathPermissionsDto userPathPermissions = userRepository.setIfAbsent(new UserPathPermissionsDto());
                UserPathPermissionDto userPathPermission = userPathPermissions.putIfAbsent(path,
                                                                                           new UserPathPermissionDto());

                userPathPermission.setPath(path + "/.*");
                userPathPermission.setPermission(pathPrivilegesToPermission(wildcardPrivilegesMap.getValue()));
            }
        }

        return userAccessModel;

    }

    private String pathPrivilegesToPermission(Collection<String> pathPrivileges)
    {
        return Collections.disjoint(pathPrivileges, Privileges.w()) ? READ : READ_WRITE;
    }
}
