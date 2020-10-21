package org.carlspring.strongbox.forms.storage.routing;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class RoutingRuleFormTestIT
        extends RestAssuredBaseTest
{

    @Inject
    private Validator validator;

    @Test
    void testRuleSetFormValid()
    {
        // given
        RoutingRuleForm routingRuleForm = new RoutingRuleForm();
        routingRuleForm.setPattern("*");
        routingRuleForm.setType(RoutingRuleTypeEnum.DENY);
        RoutingRuleRepositoryForm routingRuleRepositoryForm = new RoutingRuleRepositoryForm();
        routingRuleRepositoryForm.setRepositoryId("releases-with-trash");
        routingRuleForm.setRepositories(Lists.newArrayList(routingRuleRepositoryForm));

        // when
        Set<ConstraintViolation<RoutingRuleForm>> violations = validator.validate(routingRuleForm);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testRuleSetFormValidEmptyGroupRepository()
    {
        // given
        RoutingRuleForm routingRuleForm = new RoutingRuleForm();
        routingRuleForm.setPattern("*");
        routingRuleForm.setType(RoutingRuleTypeEnum.DENY);
        RoutingRuleRepositoryForm routingRuleRepositoryForm = new RoutingRuleRepositoryForm();
        routingRuleRepositoryForm.setRepositoryId("releases-with-trash");
        routingRuleForm.setRepositories(Lists.newArrayList(routingRuleRepositoryForm));
        routingRuleForm.setGroupRepositoryId(StringUtils.EMPTY);

        // when
        Set<ConstraintViolation<RoutingRuleForm>> violations = validator.validate(routingRuleForm);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testRuleSetFormInvalidRoutingRulesWithBlankPattern()
    {
        // given
        RoutingRuleForm routingRuleForm = new RoutingRuleForm();
        routingRuleForm.setPattern(StringUtils.EMPTY);
        routingRuleForm.setType(RoutingRuleTypeEnum.DENY);
        RoutingRuleRepositoryForm routingRuleRepositoryForm = new RoutingRuleRepositoryForm();
        routingRuleRepositoryForm.setRepositoryId("releases-with-trash");
        routingRuleForm.setRepositories(Lists.newArrayList(routingRuleRepositoryForm));

        // when
        Set<ConstraintViolation<RoutingRuleForm>> violations = validator.validate(routingRuleForm);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("A pattern must be specified.");
    }

    @Test
    void testRuleSetFormValidEmptyRepositories()
    {
        // given
        RoutingRuleForm routingRuleForm = new RoutingRuleForm();
        routingRuleForm.setPattern("*");
        routingRuleForm.setType(RoutingRuleTypeEnum.DENY);

        // when
        Set<ConstraintViolation<RoutingRuleForm>> violations = validator.validate(routingRuleForm);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testRuleSetFormInvalidRepositories()
    {
        // given
        RoutingRuleForm routingRuleForm = new RoutingRuleForm();
        routingRuleForm.setPattern("*");
        routingRuleForm.setType(RoutingRuleTypeEnum.DENY);
        RoutingRuleRepositoryForm routingRuleRepositoryForm = new RoutingRuleRepositoryForm();
        routingRuleRepositoryForm.setRepositoryId("");
        routingRuleRepositoryForm.setStorageId(null);
        routingRuleForm.setRepositories(Lists.newArrayList(routingRuleRepositoryForm));

        // when
        Set<ConstraintViolation<RoutingRuleForm>> violations = validator.validate(routingRuleForm);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(2);
        assertThat(violations).extracting("message").containsAnyOf("Either storageId or repositoryId must not be blank!");
    }

    @Test
    void testRuleSetFormInvalidTypeNotProvided()
    {
        // given
        RoutingRuleForm routingRuleForm = new RoutingRuleForm();
        routingRuleForm.setPattern("*");
        RoutingRuleRepositoryForm routingRuleRepositoryForm = new RoutingRuleRepositoryForm();
        routingRuleRepositoryForm.setRepositoryId("releases-with-trash");
        routingRuleForm.setRepositories(Lists.newArrayList(routingRuleRepositoryForm));

        // when
        Set<ConstraintViolation<RoutingRuleForm>> violations = validator.validate(routingRuleForm);

        // then
        assertThat(violations).as("Violations are empty!").hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("A type must be specified.");
    }
}
