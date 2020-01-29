package org.carlspring.strongbox.forms.users;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.converters.users.AccessModelFormToUserAccessModelDtoConverter;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.AccessModelDto;
import org.carlspring.strongbox.users.dto.PathPrivilegesDto;
import org.carlspring.strongbox.users.dto.RepositoryPrivilegesDto;
import org.carlspring.strongbox.users.dto.StoragePrivilegesDto;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Java6Assertions;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@IntegrationTest
public class AccessModelFormTestIT
        extends RestAssuredBaseTest
{

    private static final String STORAGE_ID_VALID = "storage0";

    private static final String REPOSITORY_ID_VALID = "releases";

    private Collection<String> privileges;

    @Inject
    private Validator validator;


    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        privileges = Lists.newArrayList(Privileges.r());
    }

    @Test
    public void shouldProperlyMapToDto()
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

        AccessModelDto userAccessModel = AccessModelFormToUserAccessModelDtoConverter.INSTANCE.convert(
                developer01AccessModel);
        assertThat(userAccessModel, CoreMatchers.notNullValue());

        Set<StoragePrivilegesDto> userStorages = userAccessModel.getStorageAuthorities();
        assertThat(userStorages, CoreMatchers.notNullValue());
        assertThat(userStorages.size(), CoreMatchers.equalTo(1));

        StoragePrivilegesDto userStorage = userStorages.iterator().next();
        assertThat(userStorage, CoreMatchers.notNullValue());

        assertThat(userStorage.getStorageId(), CoreMatchers.equalTo("storage0"));

        Set<RepositoryPrivilegesDto> userRepositories = userStorage.getRepositoryPrivileges();
        assertThat(userRepositories, CoreMatchers.notNullValue());
        assertThat(userRepositories.size(), CoreMatchers.equalTo(2));

        for (RepositoryPrivilegesDto userRepository : userRepositories)
        {
            assertThat(userRepository.getRepositoryId(),
                       CoreMatchers.anyOf(CoreMatchers.equalTo("releases"), CoreMatchers.equalTo("snapshots")));


            Set<Privileges> repositoryPrivileges = userRepository.getRepositoryPrivileges();
            Set<PathPrivilegesDto> pathPrivileges = userRepository.getPathPrivileges();

            if ("releases".equals(userRepository.getRepositoryId()))
            {
                assertThat(pathPrivileges, CoreMatchers.notNullValue());
                assertThat(pathPrivileges.size(), CoreMatchers.equalTo(4));

                for (PathPrivilegesDto pathPrivilege : pathPrivileges)
                {
                    assertThat(pathPrivilege.getPath(),
                               CoreMatchers.anyOf(CoreMatchers.equalTo("com/apache/foo"),
                                                  CoreMatchers.equalTo("org/apache/foo"),
                                                  CoreMatchers.equalTo("com/carlspring/foo"),
                                                  CoreMatchers.equalTo("org/carlspring/foo")));
                    if (pathPrivilege.getPath().startsWith("org"))
                    {
                        assertThat(pathPrivilege.getPrivileges().size(), CoreMatchers.equalTo(5));
                    }
                    else
                    {
                        assertThat(pathPrivilege.getPrivileges().size(), CoreMatchers.equalTo(2));
                    }

                    if (pathPrivilege.getPath().contains("carlspring"))
                    {
                        assertThat(pathPrivilege.isWildcard(), CoreMatchers.equalTo(true));
                    }
                    else
                    {
                        assertThat(pathPrivilege.isWildcard(), CoreMatchers.equalTo(false));
                    }
                }

                assertThat(repositoryPrivileges.size(), CoreMatchers.equalTo(2));
                assertThat(repositoryPrivileges,
                           IsCollectionContaining.hasItems(CoreMatchers.equalTo(Privileges.ARTIFACTS_RESOLVE),
                                                           CoreMatchers.equalTo(Privileges.ARTIFACTS_DEPLOY)));
            }
            if ("snapshots".equals(userRepository.getRepositoryId()))
            {
                assertThat(pathPrivileges, Matchers.emptyCollectionOf(PathPrivilegesDto.class));
                assertThat(repositoryPrivileges.size(), CoreMatchers.equalTo(1));
                assertThat(repositoryPrivileges, IsCollectionContaining.hasItems(
                        CoreMatchers.equalTo(Privileges.ARTIFACTS_DEPLOY)));
            }
        }
    }

    @Test
    void testAccessModelFormValid()
    {
        // given
        AccessModelForm accessModelForm = new AccessModelForm();
        RepositoryAccessModelForm repositoryAccessModelForm = new RepositoryAccessModelForm();
        repositoryAccessModelForm.setStorageId(STORAGE_ID_VALID);
        repositoryAccessModelForm.setRepositoryId(REPOSITORY_ID_VALID);
        repositoryAccessModelForm.setPrivileges(privileges);
        List<RepositoryAccessModelForm> repositories = Lists.newArrayList(repositoryAccessModelForm);
        accessModelForm.setRepositoriesAccess(repositories);

        // when
        Set<ConstraintViolation<RepositoryAccessModelForm>> violations = validator.validate(repositoryAccessModelForm);

        // then
        assertTrue(violations.isEmpty(), "Violations are not empty!");
    }

    @Test
    void testAccessModelFormInvalidEmptyStorageId()
    {
        // given
        RepositoryAccessModelForm repositoryAccessModelForm = new RepositoryAccessModelForm();
        repositoryAccessModelForm.setStorageId(StringUtils.EMPTY);
        repositoryAccessModelForm.setRepositoryId(REPOSITORY_ID_VALID);
        repositoryAccessModelForm.setPrivileges(privileges);

        // when
        Set<ConstraintViolation<RepositoryAccessModelForm>> violations = validator.validate(repositoryAccessModelForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        Java6Assertions.assertThat(violations).extracting("message").containsAnyOf("A storage id must be specified.");
    }

}
