package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.CommonConfig;
import org.carlspring.strongbox.StorageApiConfig;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.ConfigurationRepository;
import org.carlspring.strongbox.services.ConfigurationService;
import org.carlspring.strongbox.services.RoutingRulesService;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RuleSet;

import java.util.Collections;
import java.util.HashSet;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * Created by Bohdan on 8/6/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class RoutingRulesServiceImplTest
{

    private static final String RULE_PATTERN = "*.org.test";
    private static final String REPOSITORY_ID = "repo-id";
    public static final String GROUP_REPOSITORY = "dasdas";
    public static final String REPOSITORY_ID_2 = "repo";

    @org.springframework.context.annotation.Configuration
    @Import({
                    CommonConfig.class
    })
    public static class SpringConfig
    {

    }

    @Autowired
    private RoutingRulesService routingRulesService;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Test
    public void addAcceptedRuleSet()
            throws Exception
    {
        final RuleSet ruleSet = getRuleSet();
        final boolean added = routingRulesService.addAcceptedRuleSet(ruleSet);
        final Configuration configuration = configurationRepository.getConfiguration();

        final RuleSet addedRuleSet = configuration.getRoutingRules().getAccepted().get(GROUP_REPOSITORY);
        assertTrue(added);
        assertNotNull(addedRuleSet);
        assertEquals(1, addedRuleSet.getRoutingRules().size());
        assertTrue(addedRuleSet.getRoutingRules().get(0).getRepositories().contains(REPOSITORY_ID));
        assertEquals(1, addedRuleSet.getRoutingRules().get(0).getRepositories().size());
        assertEquals(RULE_PATTERN, addedRuleSet.getRoutingRules().get(0).getPattern());

    }

    @Test
    public void removeAcceptedRuleSet()
            throws Exception
    {
        routingRulesService.addAcceptedRuleSet(getRuleSet());

        final boolean removed = routingRulesService.removeAcceptedRuleSet(GROUP_REPOSITORY);

        final Configuration configuration = configurationRepository.getConfiguration();
        final RuleSet addedRuleSet = configuration.getRoutingRules().getAccepted().get(GROUP_REPOSITORY);
        assertTrue(removed);
        assertNull(addedRuleSet);

    }

    @Test
    public void addAcceptedRepo()
            throws Exception
    {
        routingRulesService.addAcceptedRuleSet(getRuleSet());

        final boolean added = routingRulesService.addAcceptedRepository(GROUP_REPOSITORY, getRoutingRule());
        final Configuration configuration = configurationRepository.getConfiguration();

        assertTrue(added);
        configuration.getRoutingRules().getAccepted().get(GROUP_REPOSITORY).getRoutingRules().stream().filter(
                routingRule -> routingRule.getPattern().equals(RULE_PATTERN))
                     .forEach(routingRule -> assertTrue(routingRule.getRepositories().contains(REPOSITORY_ID_2)));
    }

    @Test
    public void testRemoveAcceptedRepository()
            throws Exception
    {
        routingRulesService.addAcceptedRuleSet(getRuleSet());

        final boolean removed = routingRulesService.removeAcceptedRepository(GROUP_REPOSITORY, RULE_PATTERN,
                                                                             REPOSITORY_ID);

        final Configuration configuration = configurationRepository.getConfiguration();

        configuration.getRoutingRules().getAccepted().get(GROUP_REPOSITORY).getRoutingRules().forEach(
                routingRule -> {
                    if (routingRule.getPattern().equals(RULE_PATTERN))
                    {
                        assertFalse(routingRule.getRepositories().contains(REPOSITORY_ID));
                    }
                }
        );
        assertTrue(removed);

    }

    @Test
    public void testOverrideAcceptedRepositories()
            throws Exception
    {
        routingRulesService.addAcceptedRuleSet(getRuleSet());

        final RoutingRule rl = getRoutingRule();
        final boolean overridden = routingRulesService.overrideAcceptedRepositories(GROUP_REPOSITORY, rl);
        final Configuration configuration = configurationRepository.getConfiguration();
        configuration.getRoutingRules().getAccepted().get(GROUP_REPOSITORY).getRoutingRules().forEach(
                routingRule -> {
                    if (routingRule.getPattern().equals(rl.getPattern()))
                    {
                        assertEquals(1, routingRule.getRepositories().size());
                        assertEquals(rl.getRepositories(), routingRule.getRepositories());
                    }
                }
        );

        assertTrue(overridden);

    }

    private RoutingRule getRoutingRule()
    {
        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(RULE_PATTERN);
        routingRule.setRepositories(new HashSet<>(Collections.singletonList(REPOSITORY_ID_2)));

        return routingRule;
    }

    private RuleSet getRuleSet()
    {
        final RuleSet ruleSet = new RuleSet();
        ruleSet.setGroupRepository(GROUP_REPOSITORY);
        final RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(RULE_PATTERN);
        routingRule.setRepositories(new HashSet<>(Collections.singletonList(REPOSITORY_ID)));
        ruleSet.setRoutingRules(Collections.singletonList(routingRule));
        return ruleSet;
    }
}