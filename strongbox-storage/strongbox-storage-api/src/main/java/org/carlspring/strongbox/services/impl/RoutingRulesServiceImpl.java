package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationRepository;
import org.carlspring.strongbox.services.RoutingRulesService;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by bhliva on 8/6/2016.
 */
@Service
@Transactional
public class RoutingRulesServiceImpl
        implements RoutingRulesService
{

    @Autowired
    ConfigurationRepository configurationRepository;


    @Override
    public boolean addAcceptedRuleSet(RuleSet ruleSet)
    {
        final Configuration configuration = getConfiguration();
        if (configuration.getRoutingRules() == null)
        {
            configuration.setRoutingRules(new RoutingRules());
        }
        configuration.getRoutingRules().addAcceptRule(ruleSet.getGroupRepository(), ruleSet);
        updateConfiguration(configuration);

        return true;
    }

    @Override
    public boolean removeAcceptedRuleSet(String groupRepository)
    {
        final Configuration configuration = getConfiguration();
        final Map<String, RuleSet> accepted = configuration.getRoutingRules().getAccepted();
        boolean result = false;
        if (accepted.containsKey(groupRepository))
        {
            result = true;
            accepted.remove(groupRepository);
        }
        updateConfiguration(configuration);

        return result;
    }

    @Override
    public boolean addAcceptedRepository(String groupRepository,
                                         RoutingRule routingRule)
    {
        final Configuration configuration = getConfiguration();
        final Map<String, RuleSet> acceptedRulesMap = configuration.getRoutingRules().getAccepted();
        boolean added = false;
        if (acceptedRulesMap.containsKey(groupRepository))
        {
            for (RoutingRule rl : acceptedRulesMap.get(groupRepository).getRoutingRules())
            {
                if (routingRule.getPattern().equals(rl.getPattern()))
                {
                    added = true;
                    rl.getRepositories().addAll(routingRule.getRepositories());
                }
            }
        }
        updateConfiguration(configuration);

        return added;
    }

    @Override
    public boolean removeAcceptedRepository(String groupRepository,
                                            String pattern,
                                            String repositoryId)
    {
        final Configuration configuration = getConfiguration();
        final Map<String, RuleSet> acceptedRules = configuration.getRoutingRules().getAccepted();
        boolean removed = false;
        if (acceptedRules.containsKey(groupRepository))
        {
            for (RoutingRule routingRule : acceptedRules.get(groupRepository).getRoutingRules())
            {
                if (pattern.equals(routingRule.getPattern()))
                {
                    removed = true;
                    routingRule.getRepositories().remove(repositoryId);
                }
            }
        }
        updateConfiguration(configuration);

        return removed;
    }

    @Override
    public boolean overrideAcceptedRepositories(String groupRepository,
                                                RoutingRule routingRule)
    {
        final Configuration configuration = getConfiguration();
        boolean overridden = false;
        if (configuration.getRoutingRules().getAccepted().containsKey(groupRepository))
        {
            for (RoutingRule rule : configuration.getRoutingRules().getAccepted().get(groupRepository).getRoutingRules())
            {
                if (routingRule.getPattern().equals(rule.getPattern()))
                {
                    overridden = true;
                    rule.setRepositories(routingRule.getRepositories());
                }
            }
        }
        updateConfiguration(configuration);

        return overridden;
    }

    @Override
    public RoutingRules getRoutingRules()
    {
        return getConfiguration().getRoutingRules();
    }

    private void updateConfiguration(Configuration configuration)
    {
        configurationRepository.updateConfiguration(configuration);
    }

    private Configuration getConfiguration()
    {
        return configurationRepository.getConfiguration();
    }
}
