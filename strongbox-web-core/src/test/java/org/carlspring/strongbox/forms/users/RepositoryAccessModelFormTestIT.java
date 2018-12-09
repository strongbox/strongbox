package org.carlspring.strongbox.forms.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.Privileges;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;


/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class RepositoryAccessModelFormTestIT
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
    void testRepositoryAccessModelFormValid()
    {
        // given
        RepositoryAccessModelForm repositoryAccessModelForm = new RepositoryAccessModelForm();
        repositoryAccessModelForm.setStorageId(STORAGE_ID_VALID);
        repositoryAccessModelForm.setRepositoryId(REPOSITORY_ID_VALID);
        repositoryAccessModelForm.setPrivileges(privileges);

        // when
        Set<ConstraintViolation<RepositoryAccessModelForm>> violations = validator.validate(repositoryAccessModelForm);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testRepositoryAccessModelFormInvalidEmptyStorageId()
    {
        // given
        RepositoryAccessModelForm repositoryAccessModelForm = new RepositoryAccessModelForm();
        repositoryAccessModelForm.setStorageId(StringUtils.EMPTY);
        repositoryAccessModelForm.setRepositoryId(REPOSITORY_ID_VALID);
        repositoryAccessModelForm.setPrivileges(privileges);

        // when
        Set<ConstraintViolation<RepositoryAccessModelForm>> violations = validator.validate(repositoryAccessModelForm);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("A storage id must be specified.");
    }

    @Test
    void testRepositoryAccessModelFormInvalidEmptyRepositoryId()
    {
        // given
        RepositoryAccessModelForm repositoryAccessModelForm = new RepositoryAccessModelForm();
        repositoryAccessModelForm.setStorageId(STORAGE_ID_VALID);
        repositoryAccessModelForm.setRepositoryId(StringUtils.EMPTY);
        repositoryAccessModelForm.setPrivileges(privileges);

        // when
        Set<ConstraintViolation<RepositoryAccessModelForm>> violations = validator.validate(repositoryAccessModelForm);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("A repository id must be specified.");
    }

    @Test
    void testRepositoryAccessModelFormInvalidEmptyPrivileges()
    {
        // given
        RepositoryAccessModelForm repositoryAccessModelForm = new RepositoryAccessModelForm();
        repositoryAccessModelForm.setStorageId(STORAGE_ID_VALID);
        repositoryAccessModelForm.setRepositoryId(REPOSITORY_ID_VALID);
        repositoryAccessModelForm.setPrivileges(Collections.emptyList());

        // when
        Set<ConstraintViolation<RepositoryAccessModelForm>> violations = validator.validate(repositoryAccessModelForm);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("A collection of privileges must be specified.");
    }
}
