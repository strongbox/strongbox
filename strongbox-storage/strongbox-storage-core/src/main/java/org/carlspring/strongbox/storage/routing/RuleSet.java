package org.carlspring.strongbox.storage.routing;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class RuleSet
{

    private final String groupRepository;

    private final List<RoutingRule> routingRules;

    public RuleSet(final MutableRuleSet delegate)
    {
        this.groupRepository = delegate.getGroupRepository();
        this.routingRules = immuteRoutingRules(delegate.getRoutingRules());
    }

    private List<RoutingRule> immuteRoutingRules(final List<MutableRoutingRule> source)
    {
        return source != null ? ImmutableList.copyOf(source.stream().map(RoutingRule::new).collect(
                Collectors.toList())) : Collections.emptyList();
    }

    public String getGroupRepository()
    {
        return groupRepository;
    }

    public List<RoutingRule> getRoutingRules()
    {
        return routingRules;
    }
}
