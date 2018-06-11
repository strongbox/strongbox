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

import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Przemyslaw Fusik
 */
public class UsersMapperTest
{

    @Test
    public void mapperShouldWorkAsExpectedWhenMappingManagementToSecurity()
            throws Exception
    {

        MutableAccessModel developer01AccessModel = new MutableAccessModel();
        developer01AccessModel.setWildCardPrivilegesMap(
                ImmutableMap.of("/storages/storage0/releases/com/carlspring/foo", Privileges.r(),
                                "/storages/storage0/releases/org/carlspring/foo", Privileges.rw()));
        developer01AccessModel.setUrlToPrivilegesMap(
                ImmutableMap.of("/storages/storage0/releases/com/apache/foo", Privileges.r(),
                                "/storages/storage0/releases/org/apache/foo", Privileges.rw()));
        developer01AccessModel.setRepositoryPrivileges(
                ImmutableMap.of("/storages/storage0/releases", ImmutableSet.of("ARTIFACTS_RESOLVE", "ARTIFACTS_DEPLOY"),
                                "/storages/storage0/snapshots", ImmutableSet.of("ARTIFACTS_DEPLOY")));

        MutableUser developer01 = new MutableUser();
        developer01.setPassword("$2a$10$WqtVx7Iio0cndyR1lEaKW.SWhUYmF/zHHG5hkAXvH5hUmklM7QfMO");
        developer01.setUsername("developer01");
        developer01.setRoles(ImmutableSet.of("UI_MANAGER"));
        developer01.setSecurityTokenKey("developer01-secret");
        developer01.setAccessModel(developer01AccessModel);

        MutableUsers users = new MutableUsers(ImmutableSet.of(developer01));

        UsersDto securityUsers = UsersMapper.managementToSecurity(users);
        Assert.assertThat(securityUsers, CoreMatchers.notNullValue());

        Set<UserDto> userSet = securityUsers.getUsers();
        Assert.assertThat(userSet.size(), CoreMatchers.equalTo(1));

        UserDto user = userSet.iterator().next();
        Assert.assertThat(user.getUsername(), CoreMatchers.equalTo("developer01"));
        Assert.assertThat(user.getPassword(),
                          CoreMatchers.equalTo("$2a$10$WqtVx7Iio0cndyR1lEaKW.SWhUYmF/zHHG5hkAXvH5hUmklM7QfMO"));
        Assert.assertThat(user.getRoles().size(), CoreMatchers.equalTo(1));
        Assert.assertThat(user.getRoles().iterator().next(), CoreMatchers.equalTo("UI_MANAGER"));
        Assert.assertThat(user.getSecurityTokenKey(), CoreMatchers.equalTo("developer01-secret"));

        UserAccessModelDto userAccessModel = user.getUserAccessModel();
        Assert.assertThat(userAccessModel, CoreMatchers.notNullValue());

        Set<UserStorageDto> userStorages = userAccessModel.getStorages();
        Assert.assertThat(userStorages, CoreMatchers.notNullValue());
        Assert.assertThat(userStorages.size(), CoreMatchers.equalTo(1));

        UserStorageDto userStorage = userStorages.iterator().next();
        Assert.assertThat(userStorage, CoreMatchers.notNullValue());

        Assert.assertThat(userStorage.getStorageId(), CoreMatchers.equalTo("storage0"));

        Set<UserRepositoryDto> userRepositories = userStorage.getRepositories();
        Assert.assertThat(userRepositories, CoreMatchers.notNullValue());
        Assert.assertThat(userRepositories.size(), CoreMatchers.equalTo(2));

        for (UserRepositoryDto userRepository : userRepositories)
        {
            Assert.assertThat(userRepository.getRepositoryId(),
                              CoreMatchers.anyOf(CoreMatchers.equalTo("releases"), CoreMatchers.equalTo("snapshots")));

            UserPathPermissionsDto userPathPermissions = userRepository.getPathPermissions();
            Set<PrivilegeDto> privileges = userRepository.getPrivileges();

            if ("releases".equals(userRepository.getRepositoryId()))
            {
                Assert.assertThat(userPathPermissions, CoreMatchers.notNullValue());
                Set<UserPathPermissionDto> permissions = userPathPermissions.getPathPermissions();
                Assert.assertThat(permissions.size(), CoreMatchers.equalTo(4));

                for (UserPathPermissionDto permission : permissions)
                {
                    Assert.assertThat(permission.getPath(), CoreMatchers.anyOf(CoreMatchers.equalTo("com/apache/foo"),
                                                                               CoreMatchers.equalTo("org/apache/foo"),
                                                                               CoreMatchers.equalTo(
                                                                                       "com/carlspring/foo/.*"),
                                                                               CoreMatchers.equalTo(
                                                                                       "org/carlspring/foo/.*")));
                    if (permission.getPath().startsWith("org"))
                    {
                        Assert.assertThat(permission.getPermission(), CoreMatchers.equalTo("rw"));
                    }
                    else
                    {
                        Assert.assertThat(permission.getPermission(), CoreMatchers.equalTo("r"));
                    }
                }

                Assert.assertThat(privileges.size(), CoreMatchers.equalTo(2));
                Assert.assertThat(privileges, IsCollectionContaining.hasItems(
                        CoreMatchers.equalTo(new PrivilegeDto("ARTIFACTS_RESOLVE", null)),
                        CoreMatchers.equalTo(new PrivilegeDto("ARTIFACTS_DEPLOY", null))));
            }
            if ("snapshots".equals(userRepository.getRepositoryId()))
            {
                Assert.assertThat(userPathPermissions, CoreMatchers.nullValue());
                Assert.assertThat(privileges.size(), CoreMatchers.equalTo(1));
                Assert.assertThat(privileges, IsCollectionContaining.hasItems(
                        CoreMatchers.equalTo(new PrivilegeDto("ARTIFACTS_DEPLOY", null))));
            }
        }
    }

}
