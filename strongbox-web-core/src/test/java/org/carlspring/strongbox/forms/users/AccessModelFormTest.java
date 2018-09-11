package org.carlspring.strongbox.forms.users;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;
import org.carlspring.strongbox.converters.users.AccessModelFormToUserAccessModelDtoConverter;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserPathPrivilegesDto;
import org.carlspring.strongbox.users.dto.UserRepositoryDto;
import org.carlspring.strongbox.users.dto.UserStorageDto;

import java.util.Set;

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

        RepositoryAccessModelForm form = new RepositoryAccessModelForm();
        form.setStorageId("storage0");
        form.setRepositoryId("releases");
        form.setPath("com/carlspring/foo");
        form.setPrivileges(Privileges.r());
        form.setWildcard(true);
        developer01AccessModel.addRepositoryAccess(form);

        form = new RepositoryAccessModelForm();
        form.setStorageId("storage0");
        form.setRepositoryId("releases");
        form.setPath("org/carlspring/foo");
        form.setPrivileges(Privileges.rw());
        form.setWildcard(true);
        developer01AccessModel.addRepositoryAccess(form);

        form = new RepositoryAccessModelForm();
        form.setStorageId("storage0");
        form.setRepositoryId("releases");
        form.setPath("com/apache/foo");
        form.setPrivileges(Privileges.r());
        developer01AccessModel.addRepositoryAccess(form);

        form = new RepositoryAccessModelForm();
        form.setStorageId("storage0");
        form.setRepositoryId("releases");
        form.setPath("org/apache/foo");
        form.setPrivileges(Privileges.rw());
        developer01AccessModel.addRepositoryAccess(form);

        form = new RepositoryAccessModelForm();
        form.setStorageId("storage0");
        form.setRepositoryId("releases");
        form.setPrivileges(ImmutableSet.of("ARTIFACTS_RESOLVE", "ARTIFACTS_DEPLOY"));
        developer01AccessModel.addRepositoryAccess(form);

        form = new RepositoryAccessModelForm();
        form.setStorageId("storage0");
        form.setRepositoryId("snapshots");
        form.setPrivileges(ImmutableSet.of("ARTIFACTS_DEPLOY"));
        developer01AccessModel.addRepositoryAccess(form);

        UserAccessModelDto userAccessModel = AccessModelFormToUserAccessModelDtoConverter.INSTANCE.convert(
                developer01AccessModel);
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


            Set<PrivilegeDto> privileges = userRepository.getRepositoryPrivileges();
            Set<UserPathPrivilegesDto> permissions = userRepository.getPathPrivileges();

            if ("releases".equals(userRepository.getRepositoryId()))
            {
                Assert.assertThat(permissions, CoreMatchers.notNullValue());
                Assert.assertThat(permissions.size(), CoreMatchers.equalTo(4));

                for (UserPathPrivilegesDto permission : permissions)
                {
                    Assert.assertThat(permission.getPath(), CoreMatchers.anyOf(CoreMatchers.equalTo("com/apache/foo"),
                                                                               CoreMatchers.equalTo("org/apache/foo"),
                                                                               CoreMatchers.equalTo(
                                                                                       "com/carlspring/foo/.*"),
                                                                               CoreMatchers.equalTo(
                                                                                       "org/carlspring/foo/.*")));
                    if (permission.getPath().startsWith("org"))
                    {
                        Assert.assertThat(permission.getPrivileges(), CoreMatchers.equalTo("rw"));
                    }
                    else
                    {
                        Assert.assertThat(permission.getPrivileges(), CoreMatchers.equalTo("r"));
                    }
                }

                Assert.assertThat(privileges.size(), CoreMatchers.equalTo(2));
                Assert.assertThat(privileges, IsCollectionContaining.hasItems(
                        CoreMatchers.equalTo(new PrivilegeDto("ARTIFACTS_RESOLVE", null)),
                        CoreMatchers.equalTo(new PrivilegeDto("ARTIFACTS_DEPLOY", null))));
            }
            if ("snapshots".equals(userRepository.getRepositoryId()))
            {
                Assert.assertThat(permissions, CoreMatchers.nullValue());
                Assert.assertThat(privileges.size(), CoreMatchers.equalTo(1));
                Assert.assertThat(privileges, IsCollectionContaining.hasItems(
                        CoreMatchers.equalTo(new PrivilegeDto("ARTIFACTS_DEPLOY", null))));
            }
        }
    }

}
