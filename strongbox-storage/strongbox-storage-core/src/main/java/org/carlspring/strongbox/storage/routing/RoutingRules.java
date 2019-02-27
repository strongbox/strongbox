package org.carlspring.strongbox.storage.routing;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import static java.util.stream.Collectors.toList;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class RoutingRules
{

    private final List<RuleSet> accepted;

    private final List<RuleSet> denied;

    public RoutingRules(final MutableRoutingRules delegate)
    {
        this.accepted = immuteRuleSet(delegate.getAccepted());
        this.denied = immuteRuleSet(delegate.getDenied());
    }

    private List<RuleSet> immuteRuleSet(final List<MutableRuleSet> source)
    {
        return source != null ? ImmutableList.copyOf(source.stream().map(RuleSet::new).collect(toList())) :
               Collections.emptyList();
    }

    public List<RuleSet> getAccepted()
    {
        return accepted;
    }

    public List<RuleSet> getDenied()
    {
        return denied;
    }
}
