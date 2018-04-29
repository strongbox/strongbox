package org.carlspring.strongbox.converters.storage.routing;

import org.carlspring.strongbox.forms.storage.routing.RoutingRuleForm;
import org.carlspring.strongbox.storage.routing.RoutingRule;

import org.springframework.core.convert.converter.Converter;

public class RoutingRuleFormToRoutingRuleConverter
        implements Converter<RoutingRuleForm, RoutingRule>
{

    @Override
    public RoutingRule convert(RoutingRuleForm routingRuleForm)
    {
        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(routingRuleForm.getPattern());
        routingRule.setRepositories(routingRuleForm.getRepositories());

        return routingRule;
    }
}
