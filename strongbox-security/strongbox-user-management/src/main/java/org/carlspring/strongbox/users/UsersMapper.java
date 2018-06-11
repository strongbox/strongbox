package org.carlspring.strongbox.users;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.dto.UserPathPermissionDto;
import org.carlspring.strongbox.users.dto.UserPathPermissionsDto;
import org.carlspring.strongbox.users.dto.UserRepositoryDto;
import org.carlspring.strongbox.users.dto.UserStorageDto;
import org.carlspring.strongbox.users.domain.MutableAccessModel;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.domain.MutableUser;
import org.carlspring.strongbox.users.domain.MutableUsers;
import org.carlspring.strongbox.users.dto.UsersDto;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import static org.carlspring.strongbox.users.domain.Privileges.ARTIFACTS_DEPLOY;
import static org.carlspring.strongbox.users.domain.Privileges.READ;
import static org.carlspring.strongbox.users.domain.Privileges.READ_WRITE;

/**
 * @author Przemyslaw Fusik
 */
public class UsersMapper
{

    public static MutableUsers securityToManagement(final UsersDto users)
    {
        if (users == null)
        {
            return null;
        }
        return new MutableUsers(users.getUsers().stream().map(UsersMapper::toInternalUser).collect(Collectors.toSet()));
    }

    public static UsersDto managementToSecurity(final MutableUsers users)
    {
        if (users == null)
        {
            return null;
        }
        return new UsersDto(users.getUsers().stream().map(
                UsersMapper::toSecurityUser).collect(Collectors.toSet()));
    }

    private static UserDto toSecurityUser(MutableUser user)
    {
        UserDto securityUser = new UserDto();
        securityUser.setUsername(user.getUsername());
        securityUser.setPassword(user.getPassword());
        securityUser.setRoles(user.getRoles());
        securityUser.setSecurityTokenKey(user.getSecurityTokenKey());

        MutableAccessModel accessModel = user.getAccessModel();
        if (accessModel != null)
        {
            UserAccessModelDto userAccessModel = new UserAccessModelDto();

            // repositoryPrivileges
            if (accessModel.getRepositoryPrivileges() != null)
            {
                for (Map.Entry<String, Collection<String>> repositoryPrivileges : accessModel.getRepositoryPrivileges().entrySet())
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
            if (accessModel.getUrlToPrivilegesMap() != null)
            {
                for (Map.Entry<String, Collection<String>> urlToPrivileges : accessModel.getUrlToPrivilegesMap().entrySet())
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
                    userPathPermission.setPermission(
                            urlToPrivileges.getValue().contains(ARTIFACTS_DEPLOY.name()) ? READ_WRITE : READ);
                }
            }

            // wildCardPrivilegesMap
            if (accessModel.getWildCardPrivilegesMap() != null)
            {
                for (Map.Entry<String, Collection<String>> wildcardPrivilegesMap : accessModel.getWildCardPrivilegesMap().entrySet())
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
                    userPathPermission.setPermission(
                            wildcardPrivilegesMap.getValue().contains(ARTIFACTS_DEPLOY.name()) ? READ_WRITE : READ);
                }
            }

            securityUser.setUserAccessModel(userAccessModel);
        }

        return securityUser;
    }

    private static MutableUser toInternalUser(UserDto user)
    {
        MutableUser internalUser = new MutableUser();
        internalUser.setUsername(user.getUsername());
        internalUser.setPassword(user.getPassword());
        internalUser.setEnabled(true);
        internalUser.setRoles(user.getRoles());

        // load userAccessModel
        UserAccessModelDto userAccessModel = user.getUserAccessModel();
        if (userAccessModel != null)
        {
            MutableAccessModel internalAccessModel = new MutableAccessModel();
            userAccessModel.getStorages()
                           .forEach(storage ->
                                            storage.getRepositories()
                                                   .forEach(repository -> processRepository(internalAccessModel,
                                                                                            storage.getStorageId(),
                                                                                            repository)));
            internalUser.setAccessModel(internalAccessModel);
        }

        if (StringUtils.isNotBlank(user.getSecurityTokenKey()))
        {
            internalUser.setSecurityTokenKey(user.getSecurityTokenKey());
        }

        return internalUser;
    }

    private static void processRepository(MutableAccessModel internalAccessModel,
                                          String storageId,
                                          UserRepositoryDto repository)
    {
        // assign default repository-level privileges set
        Set<String> defaultPrivileges = new HashSet<>();
        String key = "/storages/" + storageId + "/" + repository.getRepositoryId();

        repository.getPrivileges()
                  .forEach(privilege -> defaultPrivileges.add(privilege.getName().toUpperCase()));

        internalAccessModel.getRepositoryPrivileges().put(key, defaultPrivileges);

        // assign path-specific privileges
        UserPathPermissionsDto userPathPermissions = repository.getPathPermissions();
        if (userPathPermissions != null)
        {

            userPathPermissions
                    .getPathPermissions()
                    .forEach(pathPermission ->
                             {
                                 Set<String> privileges = translateToPrivileges(pathPermission.getPermission());
                                 internalAccessModel.getUrlToPrivilegesMap()
                                                    .put(key + "/" + pathPermission.getPath(), privileges);
                             });
            internalAccessModel.obtainPrivileges();
        }
    }

    private static Set<String> translateToPrivileges(String permission)
    {
        if (permission == null || permission.equalsIgnoreCase(Privileges.DEFAULT))
        {
            return Privileges.rw();
        }
        else
        {
            return Privileges.r();
        }
    }

}
