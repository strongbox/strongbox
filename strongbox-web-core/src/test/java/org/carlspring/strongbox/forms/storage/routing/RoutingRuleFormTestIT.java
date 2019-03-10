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
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
        assertTrue(violations.isEmpty(), "Violations are not empty!");
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
        routingRuleForm.setRepositoryId(StringUtils.EMPTY);

        // when
        Set<ConstraintViolation<RoutingRuleForm>> violations = validator.validate(routingRuleForm);

        // then
        assertTrue(violations.isEmpty(), "Violations are not empty!");
    }

    @Test
    void testRuleSetFormInvalidRoutingRulesWithEmptyPattern()
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
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf("A pattern must be specified.");
    }

    @Test
    void testRuleSetFormInvalidEmptyRepositories()
    {
        // given
        RoutingRuleForm routingRuleForm = new RoutingRuleForm();
        routingRuleForm.setPattern("*");
        routingRuleForm.setType(RoutingRuleTypeEnum.DENY);

        // when
        Set<ConstraintViolation<RoutingRuleForm>> violations = validator.validate(routingRuleForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf("A list of repositories must be specified.");
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
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf("A type must be specified.");
    }
}
