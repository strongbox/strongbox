package org.carlspring.strongbox.forms.users;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserPathPermissionDto;
import org.carlspring.strongbox.users.dto.UserPathPermissionsDto;
import org.carlspring.strongbox.users.dto.UserRepositoryDto;
import org.carlspring.strongbox.users.dto.UserStorageDto;

import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Przemyslaw Fusik
 */
public class AccessModelFormTest
{

    @Test
    public void shouldProperlyMapToDto()
            throws Exception
    {

        AccessModelForm developer01AccessModel = new AccessModelForm();

        developer01AccessModel.setWildCardPrivileges(
            ImmutableList.of(
                new PathPrivilege("/storages/storage0/releases/com/carlspring/foo", Privileges.r()),
                new PathPrivilege("/storages/storage0/releases/org/carlspring/foo", Privileges.rw())
            )
        );

        developer01AccessModel.setUrlToPrivileges(
            ImmutableList.of(
                new PathPrivilege("/storages/storage0/releases/com/apache/foo", Privileges.r()),
                new PathPrivilege("/storages/storage0/releases/org/apache/foo", Privileges.rw())
            )
        );

        developer01AccessModel.setRepositoryPrivileges(
            ImmutableList.of(
                new PathPrivilege("/storages/storage0/releases", ImmutableSet.of("ARTIFACTS_RESOLVE", "ARTIFACTS_DEPLOY")),
                new PathPrivilege("/storages/storage0/snapshots", ImmutableSet.of("ARTIFACTS_DEPLOY"))
            )
        );

        UserAccessModelDto userAccessModel = developer01AccessModel.toDto();
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
