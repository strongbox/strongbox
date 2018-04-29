package org.carlspring.strongbox.converters.storage.routing;

import org.carlspring.strongbox.forms.storage.routing.RoutingRuleForm;
import org.carlspring.strongbox.forms.storage.routing.RuleSetForm;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RuleSet;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public class RuleSetFormToRuleSetConverter
        implements Converter<RuleSetForm, RuleSet>
{

    @Override
    public RuleSet convert(RuleSetForm ruleSetForm)
    {
        RuleSet ruleSet = new RuleSet();
        ruleSet.setGroupRepository(ruleSetForm.getGroupRepository());
        List<RoutingRule> routingRulesList = new ArrayList<>();
        for (RoutingRuleForm routingRuleForm : ruleSetForm.getRoutingRules())
        {
            RoutingRule routingRule = new RoutingRule();
            routingRule.setPattern(routingRuleForm.getPattern());
            routingRule.setRepositories(routingRuleForm.getRepositories());
            routingRulesList.add(routingRule);
        }
        ruleSet.setRoutingRules(routingRulesList);

        return ruleSet;
    }
}