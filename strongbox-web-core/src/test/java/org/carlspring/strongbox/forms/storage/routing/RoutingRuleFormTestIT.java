package org.carlspring.strongbox.forms.storage.routing;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class RoutingRuleFormTestIT
        extends RestAssuredBaseTest
{

    private static final String PATTERN_VALID = "*";
    private Set<String> repositories;

    @Inject
    private Validator validator;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        repositories = Sets.newHashSet("releases-with-trash");
    }

    @Test
    void testRoutingRuleFormValid()
    {
        // given
        RoutingRuleForm routingRuleForm = new RoutingRuleForm();
        routingRuleForm.setPattern(PATTERN_VALID);
        routingRuleForm.setRepositories(repositories);

        // when
        Set<ConstraintViolation<RoutingRuleForm>> violations = validator.validate(routingRuleForm);

        // then
        assertTrue(violations.isEmpty(), "Violations are not empty!");
    }

    @Test
    void testRoutingRuleFormInvalidEmptyPattern()
    {
        // given
        RoutingRuleForm routingRuleForm = new RoutingRuleForm();
        routingRuleForm.setPattern(StringUtils.EMPTY);
        routingRuleForm.setRepositories(repositories);

        // when
        Set<ConstraintViolation<RoutingRuleForm>> violations = validator.validate(routingRuleForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf("A pattern must be specified.");
    }

    @Test
    void testRoutingRuleFormInvalidEmptyRepositories()
    {
        // given
        RoutingRuleForm routingRuleForm = new RoutingRuleForm();
        routingRuleForm.setPattern(PATTERN_VALID);
        routingRuleForm.setRepositories(Sets.newHashSet());

        // when
        Set<ConstraintViolation<RoutingRuleForm>> violations = validator.validate(routingRuleForm);

        // then
        assertFalse(violations.isEmpty(), "Violations are empty!");
        assertEquals(violations.size(), 1);
        assertThat(violations).extracting("message").containsAnyOf("A set of repositories must be specified.");
    }
}
