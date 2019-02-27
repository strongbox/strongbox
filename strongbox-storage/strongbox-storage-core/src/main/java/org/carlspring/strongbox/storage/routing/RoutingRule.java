package org.carlspring.strongbox.storage.routing;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class RoutingRule
{

    private final String pattern;

    private final Pattern regex;

    private final List<RoutingRuleRepository> repositories;

    public RoutingRule(final MutableRoutingRule delegate)
    {
        this.pattern = delegate.getPattern();
        this.regex = Pattern.compile(pattern);
        this.repositories = immuteRepositories(delegate.getRepositories());
    }


    private List<RoutingRuleRepository> immuteRepositories(List<MutableRoutingRuleRepository> source)
    {
        return source != null ? ImmutableList.copyOf(source.stream().map(RoutingRuleRepository::new).collect(
                Collectors.toList())) : Collections.emptyList();
    }

    public String getPattern()
    {
        return pattern;
    }

    public Pattern getRegex()
    {
        return regex;
    }

    public List<RoutingRuleRepository> getRepositories()
    {
        return repositories;
    }
}
