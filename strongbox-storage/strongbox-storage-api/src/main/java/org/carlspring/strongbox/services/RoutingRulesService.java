package org.carlspring.strongbox.services;

import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;

import java.util.Set;

/**
 * Created by Bohdan on 8/6/2016.
 */
public interface RoutingRulesService
{

    boolean addAcceptedRuleSet(RuleSet ruleSet);

    boolean removeAcceptedRuleSet(String groupRepository);

    boolean addAcceptedRepository(String groupRepository,
                                  RoutingRule routingRule);

    boolean removeAcceptedRepository(String groupRepository,
                                     String pattern,
                                     String repositoryId);

    boolean overrideAcceptedRepositories(String groupRepository,
                                         RoutingRule routingRule);

    RoutingRules getRoutingRules();
}
