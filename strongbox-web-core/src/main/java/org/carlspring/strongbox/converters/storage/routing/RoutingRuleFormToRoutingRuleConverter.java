package org.carlspring.strongbox.converters.storage.routing;

import org.carlspring.strongbox.forms.storage.routing.RoutingRuleForm;

import org.springframework.core.convert.converter.Converter;

public class RoutingRuleFormToRoutingRuleConverter
        implements Converter<RoutingRuleForm, MutableRoutingRuleOld>
{

    @Override
    public MutableRoutingRuleOld convert(RoutingRuleForm routingRuleForm)
    {
        MutableRoutingRuleOld routingRule = new MutableRoutingRuleOld();
        routingRule.setPattern(routingRuleForm.getPattern());
        routingRule.setRepositories(routingRuleForm.getRepositories());

        return routingRule;
    }
}
