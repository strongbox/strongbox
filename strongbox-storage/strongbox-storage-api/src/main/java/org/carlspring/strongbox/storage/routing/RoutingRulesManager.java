package org.carlspring.strongbox.storage.routing;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author mtodorov
 */
@Component
public class RoutingRulesManager
{

    public static final String WILDCARD = "*";

    @Autowired
    private ConfigurationManager configurationManager;

    private HashMap<String, Set<RoutingRule>> accepted = new HashMap<>();

    private HashMap<String, Set<RoutingRule>> denied = new HashMap<>();
    
    
    public RoutingRulesManager()
    {
    }

    public void initialize()
    {
        if (getConfiguration().getRoutingRules() != null)
        {
            for (RoutingRule rule : getConfiguration().getRoutingRules())
            {
                if (rule.getType().equals(RoutingRuleTypeEnum.ACCEPT.getType()))
                {
                    addAcceptRule(rule);
                }
                else
                {
                    addDenyRule(rule);
                }
            }
        }
    }

    public void setAccepted(HashMap<String, Set<RoutingRule>> accepted)
    {
        this.accepted = accepted;
    }

    public HashMap<String, Set<RoutingRule>> getDenied()
    {
        return denied;
    }

    public void setDenied(HashMap<String, Set<RoutingRule>> denied)
    {
        this.denied = denied;
    }

    public void addAcceptRule(RoutingRule rule)
    {
        if (accepted.containsKey(rule.getGroupRepository()))
        {
            accepted.get(rule.getGroupRepository()).add(rule);
        }
        else
        {
            Set<RoutingRule> set = new LinkedHashSet<>();
            set.add(rule);

            accepted.put(rule.getGroupRepository(), set);
        }
    }

    public void removeAcceptRule(RoutingRule rule)
    {
        if (accepted.containsKey(rule.getGroupRepository()))
        {
            accepted.get(rule.getGroupRepository()).remove(rule);
        }
    }

    public Set<RoutingRule> getAcceptRules(String groupRepositoryId)
    {
        if (accepted.containsKey(groupRepositoryId))
        {
            return accepted.get(groupRepositoryId);
        }

        return Collections.emptySet();
    }

    public void addDenyRule(RoutingRule rule)
    {
        if (denied.containsKey(rule.getGroupRepository()))
        {
            denied.get(rule.getGroupRepository()).add(rule);
        }
        else
        {
            Set<RoutingRule> set = new LinkedHashSet<>();
            set.add(rule);

            denied.put(rule.getGroupRepository(), set);
        }
    }

    public void removeDenyRule(RoutingRule rule)
    {
        if (denied.containsKey(rule.getGroupRepository()))
        {
            denied.get(rule.getGroupRepository()).remove(rule);
        }
    }

    public Set<RoutingRule> getWildcardAcceptRules()
    {
        return getAcceptRules(WILDCARD);
    }

    public Set<RoutingRule> getWildcardDenyRules()
    {
        return getDenyRules(WILDCARD);
    }

    public Set<RoutingRule> getDenyRules(String groupRepositoryId)
    {
        if (denied.containsKey(groupRepositoryId))
        {
            return denied.get(groupRepositoryId);
        }

        return Collections.emptySet();
    }

    public ConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public HashMap<String, Set<RoutingRule>> getAccepted()
    {
        return accepted;
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
