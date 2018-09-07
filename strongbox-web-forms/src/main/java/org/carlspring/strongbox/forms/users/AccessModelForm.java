package org.carlspring.strongbox.forms.users;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserPathPermissionDto;
import org.carlspring.strongbox.users.dto.UserPathPermissionsDto;
import org.carlspring.strongbox.users.dto.UserRepositoryDto;
import org.carlspring.strongbox.users.dto.UserStorageDto;
import org.carlspring.strongbox.validation.users.ValidAccessModelPath;
import org.carlspring.strongbox.validation.users.ValidAccessModelPrivilege;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import static org.carlspring.strongbox.users.domain.Permissions.READ;
import static org.carlspring.strongbox.users.domain.Permissions.READ_WRITE;

public class AccessModelForm
        implements Serializable
{

    @ValidAccessModelPath(message = "The repository privileges map keys must follow the pattern '/storages/{storageId}/{repositoryId}'.",
                          regexp = "/storages/{storageId}/{repositoryId}/")
    @ValidAccessModelPrivilege(message = "The repository privileges map values must be specified.")
    private List<PathPrivilege> repositoryPrivileges;

    @ValidAccessModelPath(message = "The URL to privileges map keys must follow the pattern '/storages/{storageId}/{repositoryId}/{path}'.",
                          regexp = "/storages/{storageId}/repositoryId/{path:.+}")
    @ValidAccessModelPrivilege(message = "The URL to privileges map values must be specified.")
    private List<PathPrivilege> urlToPrivileges;

    @ValidAccessModelPath(message = "The wildcard privileges map keys must follow the pattern '/storages/{storageId}/{repositoryId}/{path}'.",
                          regexp = "/storages/{storageId}/repositoryId/{path:**}")
    @ValidAccessModelPrivilege(message = "The wildcard privileges map values must be specified.")
    private List<PathPrivilege> wildCardPrivileges;

    public AccessModelForm()
    {
        repositoryPrivileges = new ArrayList<>();
        urlToPrivileges = new ArrayList<>();
        wildCardPrivileges = new ArrayList<>();
    }

    public List<PathPrivilege> getRepositoryPrivileges()
    {
        return repositoryPrivileges;
    }

    public void setRepositoryPrivileges(List<PathPrivilege> repositoryPrivileges)
    {
        this.repositoryPrivileges = repositoryPrivileges;
    }

    public List<PathPrivilege> getUrlToPrivileges()
    {
        return urlToPrivileges;
    }

    public void setUrlToPrivileges(List<PathPrivilege> urlToPrivilegesMap)
    {
        this.urlToPrivileges = urlToPrivilegesMap;
    }

    public List<PathPrivilege> getWildCardPrivileges()
    {
        return wildCardPrivileges;
    }

    public void setWildCardPrivileges(List<PathPrivilege> wildCardPrivilegesMap)
    {
        this.wildCardPrivileges = wildCardPrivilegesMap;
    }

    public UserAccessModelDto toDto()
    {
        UserAccessModelDto userAccessModel = new UserAccessModelDto();

        // repositoryPrivileges
        if (getRepositoryPrivileges() != null)
        {
            for (PathPrivilege accessModelAuthority : getRepositoryPrivileges())
            {
                String storageIdAndRepositoryId = StringUtils.substringAfter(accessModelAuthority.getPath(),
                                                                             "/storages/");
                String storageId = StringUtils.substringBefore(storageIdAndRepositoryId, "/");
                String repositoryId = StringUtils.substringAfter(storageIdAndRepositoryId, "/");

                UserStorageDto userStorage = userAccessModel.putIfAbsent(storageId, new UserStorageDto());

                UserRepositoryDto userRepository = userStorage.putIfAbsent(repositoryId, new UserRepositoryDto());

                userRepository.setPrivileges(
                        accessModelAuthority.getPrivileges()
                                            .stream()
                                            .map(name -> new PrivilegeDto(name, null))
                                            .collect(Collectors.toSet())
                );
            }
        }

        // urlToPrivileges
        if (getUrlToPrivileges() != null)
        {
            for (PathPrivilege accessModelAuthority : getUrlToPrivileges())
            {
                String storageIdRepositoryIdAndPath = StringUtils.substringAfter(accessModelAuthority.getPath(),
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
                userPathPermission.setPermission(pathPrivilegesToPermission(accessModelAuthority.getPrivileges()));
            }
        }

        // wildCardPrivileges
        if (getWildCardPrivileges() != null)
        {
            for (PathPrivilege accessModelAuthority : getWildCardPrivileges())
            {
                String storageIdRepositoryIdAndPath = StringUtils.substringAfter(accessModelAuthority.getPath(),
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
                userPathPermission.setPermission(pathPrivilegesToPermission(accessModelAuthority.getPrivileges()));
            }
        }

        return userAccessModel;

    }

    private String pathPrivilegesToPermission(Collection<String> pathPrivileges)
    {
        return Collections.disjoint(pathPrivileges, Privileges.w()) ? READ : READ_WRITE;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(AccessModelForm.class.getSimpleName() + " {\n");
        sb.append(" repositoryPrivileges=")
          .append(repositoryPrivileges);
        sb.append(",\n urlToPrivileges=")
          .append(urlToPrivileges);
        sb.append(",\n wildCardPrivileges=")
          .append(wildCardPrivileges);
        sb.append("\n}");
        return sb.toString();
    }
}
