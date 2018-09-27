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
import org.hamcrest.Matchers;
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


            Set<PrivilegeDto> repositoryPrivileges = userRepository.getRepositoryPrivileges();
            Set<UserPathPrivilegesDto> pathPrivileges = userRepository.getPathPrivileges();

            if ("releases".equals(userRepository.getRepositoryId()))
            {
                Assert.assertThat(pathPrivileges, CoreMatchers.notNullValue());
                Assert.assertThat(pathPrivileges.size(), CoreMatchers.equalTo(4));

                for (UserPathPrivilegesDto pathPrivilege : pathPrivileges)
                {
                    Assert.assertThat(pathPrivilege.getPath(),
                                      CoreMatchers.anyOf(CoreMatchers.equalTo("com/apache/foo"),
                                                         CoreMatchers.equalTo("org/apache/foo"),
                                                         CoreMatchers.equalTo("com/carlspring/foo"),
                                                         CoreMatchers.equalTo("org/carlspring/foo")));
                    if (pathPrivilege.getPath().startsWith("org"))
                    {
                        Assert.assertThat(pathPrivilege.getPrivileges().size(), CoreMatchers.equalTo(5));
                    }
                    else
                    {
                        Assert.assertThat(pathPrivilege.getPrivileges().size(), CoreMatchers.equalTo(2));
                    }

                    if (pathPrivilege.getPath().contains("carlspring"))
                    {
                        Assert.assertThat(pathPrivilege.isWildcard(), CoreMatchers.equalTo(true));
                    }
                    else
                    {
                        Assert.assertThat(pathPrivilege.isWildcard(), CoreMatchers.equalTo(false));
                    }
                }

                Assert.assertThat(repositoryPrivileges.size(), CoreMatchers.equalTo(2));
                Assert.assertThat(repositoryPrivileges, IsCollectionContaining.hasItems(
                        CoreMatchers.equalTo(new PrivilegeDto("ARTIFACTS_RESOLVE", null)),
                        CoreMatchers.equalTo(new PrivilegeDto("ARTIFACTS_DEPLOY", null))));
            }
            if ("snapshots".equals(userRepository.getRepositoryId()))
            {
                Assert.assertThat(pathPrivileges, Matchers.emptyCollectionOf(UserPathPrivilegesDto.class));
                Assert.assertThat(repositoryPrivileges.size(), CoreMatchers.equalTo(1));
                Assert.assertThat(repositoryPrivileges, IsCollectionContaining.hasItems(
                        CoreMatchers.equalTo(new PrivilegeDto("ARTIFACTS_DEPLOY", null))));
            }
        }
    }

}
