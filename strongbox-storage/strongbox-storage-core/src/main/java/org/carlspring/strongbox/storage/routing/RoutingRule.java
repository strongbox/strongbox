package org.carlspring.strongbox.storage.routing;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class RoutingRule
        implements RepositoryIdentifiable
{

    private final UUID uuid;

    private final String storageId;

    private final String repositoryId;

    private final String pattern;

    private final Pattern regex;

    private final RoutingRuleTypeEnum type;

    private final List<RoutingRuleRepository> repositories;

    public RoutingRule(final MutableRoutingRule delegate)
    {
        this.uuid = delegate.getUuid();
        this.repositoryId = delegate.getRepositoryId();
        this.storageId = delegate.getStorageId();
        this.pattern = delegate.getPattern();
        this.regex = Pattern.compile(pattern);
        this.type = RoutingRuleTypeEnum.of(delegate.getType());
        this.repositories = immuteRepositories(delegate.getRepositories());
    }

    private List<RoutingRuleRepository> immuteRepositories(List<MutableRoutingRuleRepository> source)
    {
        return source != null ? ImmutableList.copyOf(source.stream().map(RoutingRuleRepository::new).collect(
                Collectors.toList())) : Collections.emptyList();
    }

    public UUID getUuid()
    {
        return uuid;
    }

    @Override
    public String getStorageId()
    {
        return storageId;
    }

    @Override
    public String getRepositoryId()
    {
        return repositoryId;
    }

    public String getPattern()
    {
        return pattern;
    }

    public Pattern getRegex()
    {
        return regex;
    }

    public RoutingRuleTypeEnum getType()
    {
        return type;
    }

    public List<RoutingRuleRepository> getRepositories()
    {
        return repositories;
    }

    public boolean isDeny()
    {
        return type == RoutingRuleTypeEnum.DENY;
    }

    public boolean isAccept()
    {
        return type == RoutingRuleTypeEnum.ACCEPT;
    }
}
