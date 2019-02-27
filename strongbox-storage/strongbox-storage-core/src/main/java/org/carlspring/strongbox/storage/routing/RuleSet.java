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
        implements RepositoryIdentifiable
{

    private final String storageId;

    private final String repositoryId;

    private final List<RoutingRule> routingRules;

    public RuleSet(final MutableRuleSet delegate)
    {
        this.repositoryId = delegate.getRepositoryId();
        this.storageId = delegate.getStorageId();
        this.routingRules = immuteRoutingRules(delegate.getRoutingRules());
    }

    private List<RoutingRule> immuteRoutingRules(final List<MutableRoutingRule> source)
    {
        return source != null ? ImmutableList.copyOf(source.stream().map(RoutingRule::new).collect(
                Collectors.toList())) : Collections.emptyList();
    }

    public String getStorageId()
    {
        return storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public List<RoutingRule> getRoutingRules()
    {
        return routingRules;
    }
}
