package org.carlspring.strongbox.controllers.users.support;

import org.carlspring.strongbox.users.domain.AccessModel;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Przemyslaw Fusik
 */
public class AccessModelOutput
{

    @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
    private Map<String, Collection<String>> repositoryPrivileges = new LinkedHashMap<>();

    @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
    private Map<String, Collection<String>> urlToPrivilegesMap = new LinkedHashMap<>();

    @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
    private Map<String, Collection<String>> wildCardPrivilegesMap = new LinkedHashMap<>();

    public AccessModelOutput()
    {

    }

    public AccessModelOutput(final AccessModel accessModel)
    {
        if (accessModel != null)
        {
            repositoryPrivileges.putAll(accessModel.getRepositoryPrivileges());
            urlToPrivilegesMap.putAll(accessModel.getUrlToPrivilegesMap());
            wildCardPrivilegesMap.putAll(accessModel.getWildCardPrivilegesMap());
        }
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
