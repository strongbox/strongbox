package org.carlspring.strongbox.converters.storage.routing;

import org.carlspring.strongbox.forms.storage.routing.RoutingRuleForm;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;

import org.springframework.core.convert.converter.Converter;

public class RoutingRuleFormToRoutingRuleConverter
        implements Converter<RoutingRuleForm, MutableRoutingRule>
{

    @Override
    public MutableRoutingRule convert(RoutingRuleForm routingRuleForm)
    {
        MutableRoutingRule routingRule = new MutableRoutingRule();
        routingRule.setPattern(routingRuleForm.getPattern());
        routingRule.setRepositories(routingRuleForm.getRepositories());

        return routingRule;
    }
}
