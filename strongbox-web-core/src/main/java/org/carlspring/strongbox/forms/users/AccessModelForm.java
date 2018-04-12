package org.carlspring.strongbox.forms.users;

import org.carlspring.strongbox.validation.users.ValidAccessModelMapKey;
import org.carlspring.strongbox.validation.users.ValidAccessModelMapValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AccessModelForm
{

    @ValidAccessModelMapKey(message = "The repository privileges map keys must follow the pattern '/storages/{storageId}/{repositoryId}'.")
    @ValidAccessModelMapValue(message = "The repository privileges map values must be specified.")
    private Map<String, Collection<String>> repositoryPrivileges;

    @ValidAccessModelMapKey(message = "The URL to privileges map keys must follow the pattern '/storages/{storageId}/{repositoryId}'.")
    @ValidAccessModelMapValue(message = "The URL to privileges map values must be specified.")
    private Map<String, Collection<String>> urlToPrivilegesMap;

    @ValidAccessModelMapKey(message = "The wildcard privileges map keys must follow the pattern '/storages/{storageId}/{repositoryId}'.")
    @ValidAccessModelMapValue(message = "The wildcard privileges map values must be specified.")
    private Map<String, Collection<String>> wildCardPrivilegesMap;

    public AccessModelForm()
    {
        repositoryPrivileges = new HashMap<>();
        urlToPrivilegesMap = new HashMap<>();
        wildCardPrivilegesMap = new HashMap<>();
    }

    public Map<String, Collection<String>> getRepositoryPrivileges()
    {
        return repositoryPrivileges;
    }

    public void setRepositoryPrivileges(Map<String, Collection<String>> repositoryPrivileges)
    {
        this.repositoryPrivileges = repositoryPrivileges;
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
}
