package org.carlspring.strongbox.users.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Additional user settings (for example, per-repository access settings).
 *
 * @author Alex Oreshkevich
 */
public class AccessModel
{

    private static final Logger logger = LoggerFactory.getLogger(AccessModel.class);

    private Map<String, Collection<String>> repositoryPrivileges;

    // maps URL tails including wildcards for regular expressions on set of privileges that was assigned
    // example:
    //      key:    storage0/act-releases-1/pro/redsoft/bar/.*
    //      value:  {ARTIFACTS_VIEW, ARTIFACTS_RESOLVE, ARTIFACTS_DEPLOY}
    private Map<String, Collection<String>> urlToPrivilegesMap;

    private Map<String, Collection<String>> wildCardPrivilegesMap;

    public AccessModel()
    {
        repositoryPrivileges = new HashMap<>();
        urlToPrivilegesMap = new HashMap<>();
        wildCardPrivilegesMap = new HashMap<>();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessModel that = (AccessModel) o;
        return Objects.equal(urlToPrivilegesMap, that.urlToPrivilegesMap);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(urlToPrivilegesMap);
    }

    public Map<String, Collection<String>> getUrlToPrivilegesMap()
    {
        return urlToPrivilegesMap;
    }

    public void setUrlToPrivilegesMap(Map<String, Collection<String>> urlToPrivilegesMap)
    {
        this.urlToPrivilegesMap = urlToPrivilegesMap;
    }

    public Map<String, Collection<String>> getWildCardPrivilegesMap()
    {
        return wildCardPrivilegesMap;
    }

    public void setWildCardPrivilegesMap(Map<String, Collection<String>> wildCardPrivilegesMap)
    {
        this.wildCardPrivilegesMap = wildCardPrivilegesMap;
    }

    public Map<String, Collection<String>> getRepositoryPrivileges()
    {
        return repositoryPrivileges;
    }

    public void setRepositoryPrivileges(Map<String, Collection<String>> repositoryPrivileges)
    {
        this.repositoryPrivileges = repositoryPrivileges;
    }

    public void obtainPrivileges()
    {
        if (urlToPrivilegesMap != null && !urlToPrivilegesMap.isEmpty())
        {
            final List<String> keysToRemove = new LinkedList<>();
            urlToPrivilegesMap.entrySet()
                              .stream()
                              .filter(entry -> entry.getKey()
                                                    .endsWith(".*") || entry.getKey()
                                                                            .endsWith("**"))
                              .forEach(entry ->
                                       {
                                           String newUrl = entry.getKey()
                                                                .substring(0, entry.getKey()
                                                                                   .length() - 3);
                                           List<String> newPrivileges = new ArrayList<>(entry.getValue());
                                           wildCardPrivilegesMap.put(newUrl, newPrivileges);
                                           keysToRemove.add(entry.getKey());
                                       });
            keysToRemove.forEach(key -> urlToPrivilegesMap.remove(key));
            keysToRemove.clear();
        }
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

        logger.debug("Calculated privileges for \n\t" + url + "\n\t" + privileges);
        return privileges;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("\nAccessModel {");
        prettyPrintMap(sb, "urlToPrivilegesMap", urlToPrivilegesMap);
        prettyPrintMap(sb, "repositoryPrivileges", repositoryPrivileges);
        prettyPrintMap(sb, "wildCardPrivilegesMap", wildCardPrivilegesMap);
        sb.append("\n}");
        return sb.toString();
    }

    private void prettyPrintMap(StringBuilder sb,
                                String name,
                                Map<?, ?> map)
    {
        if (map != null && !map.isEmpty())
        {
            sb.append("\n\t")
              .append(name)
              .append(" = {");

            map.entrySet()
               .forEach(entry ->
                                sb.append("\n\t\t")
                                  .append(entry.getKey())
                                  .append("\t\t-> ")
                                  .append(entry.getValue()));
            sb.append("\n\t}");
        }
    }
}
