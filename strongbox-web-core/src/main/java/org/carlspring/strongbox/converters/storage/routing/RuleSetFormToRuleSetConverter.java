package org.carlspring.strongbox.converters.storage.routing;

import org.carlspring.strongbox.forms.storage.routing.RoutingRuleForm;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;
import org.carlspring.strongbox.storage.routing.MutableRoutingRuleRepository;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 * @author Przemyslaw Fusik
 */
public class RuleSetFormToRuleSetConverter
        implements Converter<RoutingRuleForm, MutableRoutingRule>
{

    @Override
    public MutableRoutingRule convert(RoutingRuleForm routingRuleForm)
    {
        MutableRoutingRule rule = new MutableRoutingRule();
        rule.setRepositoryId(StringUtils.defaultIfBlank(routingRuleForm.getRepositoryId(), "*"));
        rule.setStorageId(StringUtils.defaultIfBlank(routingRuleForm.getStorageId(), "*"));
        rule.setType(routingRuleForm.getType().getType());
        rule.setPattern(routingRuleForm.getPattern());
        rule.setRepositories(routingRuleForm.getRepositories().stream().map(r -> {
            MutableRoutingRuleRepository repository = new MutableRoutingRuleRepository();
            repository.setRepositoryId(StringUtils.defaultIfBlank(routingRuleForm.getRepositoryId(), "*"));
            repository.setStorageId(StringUtils.defaultIfBlank(routingRuleForm.getStorageId(), "*"));
            return repository;
        }).collect(Collectors.toList()));

        return rule;
    }
}
