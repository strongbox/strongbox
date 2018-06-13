package org.carlspring.strongbox.converters.storage.routing;

import org.carlspring.strongbox.forms.storage.routing.RoutingRuleForm;
import org.carlspring.strongbox.forms.storage.routing.RuleSetForm;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;
import org.carlspring.strongbox.storage.routing.MutableRuleSet;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public class RuleSetFormToRuleSetConverter
        implements Converter<RuleSetForm, MutableRuleSet>
{

    @Override
    public MutableRuleSet convert(RuleSetForm ruleSetForm)
    {
        MutableRuleSet ruleSet = new MutableRuleSet();
        ruleSet.setGroupRepository(ruleSetForm.getGroupRepository());
        List<MutableRoutingRule> routingRulesList = new ArrayList<>();
        for (RoutingRuleForm routingRuleForm : ruleSetForm.getRoutingRules())
        {
            MutableRoutingRule routingRule = new MutableRoutingRule();
            routingRule.setPattern(routingRuleForm.getPattern());
            routingRule.setRepositories(routingRuleForm.getRepositories());
            routingRulesList.add(routingRule);
        }
        ruleSet.setRoutingRules(routingRulesList);

        return ruleSet;
    }
}
