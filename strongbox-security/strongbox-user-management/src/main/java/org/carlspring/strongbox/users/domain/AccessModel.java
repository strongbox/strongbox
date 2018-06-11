package org.carlspring.strongbox.users.domain;

import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import static java.util.stream.Collectors.toMap;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class AccessModel
{

    private final Map<String, Collection<String>> repositoryPrivileges;

    private final Map<String, Collection<String>> urlToPrivilegesMap;

    private final Map<String, Collection<String>> wildCardPrivilegesMap;

    public AccessModel(final MutableAccessModel other)
    {
        this.repositoryPrivileges = immutePrivilegesMap(other.getRepositoryPrivileges());
        this.urlToPrivilegesMap = immutePrivilegesMap(other.getUrlToPrivilegesMap());
        this.wildCardPrivilegesMap = immutePrivilegesMap(other.getWildCardPrivilegesMap());
    }

    private Map<String, Collection<String>> immutePrivilegesMap(final Map<String, Collection<String>> source)
    {
        return source != null ? ImmutableMap.copyOf(source.entrySet().stream().collect(
                toMap(Map.Entry::getKey, e -> ImmutableList.copyOf(e.getValue())))) : Collections.emptyMap();
    }

    public Map<String, Collection<String>> getRepositoryPrivileges()
    {
        return repositoryPrivileges;
    }

    public Map<String, Collection<String>> getUrlToPrivilegesMap()
    {
        return urlToPrivilegesMap;
    }

    public Map<String, Collection<String>> getWildCardPrivilegesMap()
    {
        return wildCardPrivilegesMap;
    }

    public Collection<String> getPathPrivileges(String url)
    {
        Set<String> privileges = new HashSet<>();

        // if repository privileges covers URL, assign it's privileges by default
        repositoryPrivileges.forEach((key, value) ->
                                     {
                                         if (url.startsWith(key))
                                         {
                                             privileges.addAll(value);
                                         }
                                     });

        // if URL covered by exact path mapping add it's privileges
        if (urlToPrivilegesMap.containsKey(url))
        {
            privileges.addAll(urlToPrivilegesMap.get(url));
        }

        // if URL covered by wildcard mapping add it's privileges
        if (!wildCardPrivilegesMap.isEmpty())
        {
            wildCardPrivilegesMap.entrySet()
                                 .stream()
                                 .filter(entry -> url.startsWith(entry.getKey()))
                                 .findAny()
                                 .ifPresent(entry ->
                                                    privileges.addAll(entry.getValue()));
        }

        return privileges;
    }
}
