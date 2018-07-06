package org.carlspring.strongbox.users.domain;

import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserPathPermissionsDto;
import org.carlspring.strongbox.users.dto.UserRepositoryDto;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import static java.util.stream.Collectors.toMap;

import java.io.Serializable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class AccessModel implements Serializable
{

    private final Map<String, Collection<String>> repositoryPrivileges;

    private final Map<String, Collection<String>> urlToPrivilegesMap;

    private final Map<String, Collection<String>> wildCardPrivilegesMap;

    public AccessModel(final UserAccessModelDto userAccessModel)
    {
        Map<String, Collection<String>> repositoryPrivileges = new LinkedHashMap<>();
        Map<String, Collection<String>> urlToPrivilegesMap = new LinkedHashMap<>();
        Map<String, Collection<String>> wildCardPrivilegesMap = new LinkedHashMap<>();

        if (userAccessModel != null)
        {
            userAccessModel.getStorages()
                           .forEach(
                                   storage -> storage.getRepositories()
                                                     .forEach(repository -> processRepository(repositoryPrivileges,
                                                                                              urlToPrivilegesMap,
                                                                                              wildCardPrivilegesMap,
                                                                                              storage.getStorageId(),
                                                                                              repository)));
        }
        
        this.repositoryPrivileges = immutePrivilegesMap(repositoryPrivileges);
        this.urlToPrivilegesMap = immutePrivilegesMap(urlToPrivilegesMap);
        this.wildCardPrivilegesMap = immutePrivilegesMap(wildCardPrivilegesMap);
    }

    private static void processRepository(Map<String, Collection<String>> repositoryPrivileges,
                                          Map<String, Collection<String>> urlToPrivilegesMap,
                                          Map<String, Collection<String>> wildCardPrivilegesMap,
                                          String storageId,
                                          UserRepositoryDto repository)
    {
        // assign default repository-level privileges set
        Set<String> defaultPrivileges = new HashSet<>();
        String key = "/storages/" + storageId + "/" + repository.getRepositoryId();

        repository.getPrivileges()
                  .forEach(privilege -> defaultPrivileges.add(privilege.getName().toUpperCase()));

        repositoryPrivileges.put(key, defaultPrivileges);

        // assign path-specific privileges
        UserPathPermissionsDto userPathPermissions = repository.getPathPermissions();
        if (userPathPermissions != null)
        {

            userPathPermissions
                    .getPathPermissions()
                    .forEach(pathPermission ->
                             {
                                 Set<String> privileges = translateToPrivileges(pathPermission.getPermission());
                                 urlToPrivilegesMap.put(key + "/" + pathPermission.getPath(), privileges);
                             });
            toWildCardPrivilegesMap(urlToPrivilegesMap, wildCardPrivilegesMap);
        }
    }

    private static void toWildCardPrivilegesMap(Map<String, Collection<String>> urlToPrivilegesMap,
                                                Map<String, Collection<String>> wildCardPrivilegesMap)
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

    private static Set<String> translateToPrivileges(String permission)
    {
        if (permission == null || permission.equalsIgnoreCase(Permissions.DEFAULT))
        {
            return Privileges.rw();
        }
        else
        {
            return Privileges.r();
        }
    }

    private Map<String, Collection<String>> immutePrivilegesMap(final Map<String, Collection<String>> source)
    {
        return source != null ? ImmutableMap.copyOf(source.entrySet().stream().collect(
                toMap(Map.Entry::getKey, e -> ImmutableList.copyOf(e.getValue())))) : Collections.emptyMap();
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
    
}
