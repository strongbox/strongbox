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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@IntegrationTest
public class AccessModelFormTest
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
        assertThat(userAccessModel).isNotNull();

        Set<StoragePrivilegesDto> userStorages = userAccessModel.getStorageAuthorities();
        assertThat(userStorages).isNotNull();
        assertThat(userStorages).hasSize(1);

        StoragePrivilegesDto userStorage = userStorages.iterator().next();
        assertThat(userStorage).isNotNull();

        assertThat(userStorage.getStorageId()).isEqualTo("storage0");

        Set<RepositoryPrivilegesDto> userRepositories = userStorage.getRepositoryPrivileges();
        assertThat(userRepositories).isNotNull();
        assertThat(userRepositories).hasSize(2);

        for (RepositoryPrivilegesDto userRepository : userRepositories)
        {
            assertThat(userRepository.getRepositoryId()).isIn("releases", "snapshots");

            Set<Privileges> repositoryPrivileges = userRepository.getRepositoryPrivileges();
            Set<PathPrivilegesDto> pathPrivileges = userRepository.getPathPrivileges();

            if ("releases".equals(userRepository.getRepositoryId()))
            {
                assertThat(pathPrivileges).isNotNull();
                assertThat(pathPrivileges).hasSize(4);

                for (PathPrivilegesDto pathPrivilege : pathPrivileges)
                {
                    assertThat(pathPrivilege.getPath())
                            .isIn("com/apache/foo",
                                  "org/apache/foo",
                                  "com/carlspring/foo",
                                  "org/carlspring/foo");
                    if (pathPrivilege.getPath().startsWith("org"))
                    {
                        assertThat(pathPrivilege.getPrivileges()).hasSize(5);
                    }
                    else
                    {
                        assertThat(pathPrivilege.getPrivileges()).hasSize(2);
                    }

                    if (pathPrivilege.getPath().contains("carlspring"))
                    {
                        assertThat(pathPrivilege.isWildcard()).isTrue();
                    }
                    else
                    {
                        assertThat(pathPrivilege.isWildcard()).isFalse();
                    }
                }

                assertThat(repositoryPrivileges).hasSize(2);
                assertThat(repositoryPrivileges).contains(Privileges.ARTIFACTS_RESOLVE, Privileges.ARTIFACTS_DEPLOY);
            }
            if ("snapshots".equals(userRepository.getRepositoryId()))
            {
                assertThat(pathPrivileges).isEmpty();
                assertThat(repositoryPrivileges).hasSize(1);
                assertThat(repositoryPrivileges).contains(Privileges.ARTIFACTS_DEPLOY);
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
        assertThat(violations).as("Violations are not empty!").isEmpty();
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
        assertThat(violations).as("Violations are empty!").isNotEmpty();
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("A storage id must be specified.");
    }

}
