package org.carlspring.strongbox.forms.storage.routing;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
public class RuleSetFormTestIT
        extends RestAssuredBaseTest
{

    private static final String GROUP_REPOSITORY_VALID = "group-releases-2";
    private List<RoutingRuleForm> routingRules;

    @Inject
    private Validator validator;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        RoutingRuleForm routingRuleForm = new RoutingRuleForm();
        routingRuleForm.setPattern("*");
        Set<String> repositories = Sets.newHashSet("releases-with-trash");
        routingRuleForm.setRepositories(repositories);
        routingRules = Lists.newArrayList(routingRuleForm);
    }

    @Test
    void testRuleSetFormValid()
    {
        // given
        RuleSetForm ruleSetForm = new RuleSetForm();
        ruleSetForm.setGroupRepository(GROUP_REPOSITORY_VALID);
        ruleSetForm.setRoutingRules(routingRules);

        // when
        Set<ConstraintViolation<RuleSetForm>> violations = validator.validate(ruleSetForm);

        // then
        assertTrue(violations.isEmpty(), "Violations are not empty!");
    }

    @Test
    void testRuleSetFormInvalidEmptyGroupRepository()
    {
        // given
        RuleSetForm ruleSetForm = new RuleSetForm();
        ruleSetForm.setGroupRepository(StringUtils.EMPTY);
        ruleSetForm.setRoutingRules(routingRules);

        // when
        Set<ConstraintViolation<RuleSetForm>> violations = validator.validate(ruleSetForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf("A group repository must be specified.");
    }

    @Test
    void testRuleSetFormInvalidRoutingRulesWithEmptyPattern()
    {
        // given
        RuleSetForm ruleSetForm = new RuleSetForm();
        ruleSetForm.setGroupRepository(GROUP_REPOSITORY_VALID);
        routingRules.forEach(r -> r.setPattern(StringUtils.EMPTY));
        ruleSetForm.setRoutingRules(routingRules);

        // when
        Set<ConstraintViolation<RuleSetForm>> violations = validator.validate(ruleSetForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf("A pattern must be specified.");
    }

    @Test
    void testRuleSetFormInvalidEmptyRoutingRules()
    {
        // given
        RuleSetForm ruleSetForm = new RuleSetForm();
        ruleSetForm.setGroupRepository(GROUP_REPOSITORY_VALID);
        ruleSetForm.setRoutingRules(Collections.emptyList());

        // when
        Set<ConstraintViolation<RuleSetForm>> violations = validator.validate(ruleSetForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf("A list of routing rules must be specified.");
    }
}
