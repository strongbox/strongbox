package org.carlspring.strongbox.storage.routing;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import static java.util.stream.Collectors.toMap;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class RoutingRules
{

    public static final String WILDCARD = MutableRoutingRules.WILDCARD;

    private final Map<String, RuleSet> accepted;

    private final Map<String, RuleSet> denied;

    public RoutingRules(final MutableRoutingRules delegate)
    {
        this.accepted = immuteRuleSet(delegate.getAccepted());
        this.denied = immuteRuleSet(delegate.getDenied());
    }

    private Map<String, RuleSet> immuteRuleSet(final Map<String, MutableRuleSet> source)
    {
        return source != null ? ImmutableMap.copyOf(source.entrySet().stream().collect(
                toMap(Map.Entry::getKey, e -> new RuleSet(e.getValue())))) : Collections.emptyMap();
    }

    public Map<String, RuleSet> getAccepted()
    {
        return accepted;
    }

    public Map<String, RuleSet> getDenied()
    {
        return denied;
    }

    public RuleSet getWildcardAcceptedRules()
    {
        return accepted.get(WILDCARD);
    }

    public RuleSet getWildcardDeniedRules()
    {
        return denied.get(WILDCARD);
    }

    public RuleSet getAcceptRules(String groupRepositoryId)
    {
        return accepted.get(groupRepositoryId);
    }

    public RuleSet getWildcardDenyRules()
    {
        return getDenyRules(WILDCARD);
    }

    public RuleSet getDenyRules(String groupRepositoryId)
    {
        return denied.get(groupRepositoryId);
    }


}
