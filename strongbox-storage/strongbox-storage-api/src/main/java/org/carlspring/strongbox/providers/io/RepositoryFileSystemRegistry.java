package org.carlspring.strongbox.providers.io;

import java.util.HashMap;
import java.util.Map;

import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepositoryFileSystemRegistry
{

    private Map<String, RepositoryFileSystemProviderFactory> fileSystemProviderFactoryMap = new HashMap<>();

    private Map<String, RepositoryFileSystemFactory> fileSystemFactoryMap = new HashMap<>();

    @Autowired(required = false)
    public void setFyleSystemProviderFactories(Map<String, RepositoryFileSystemProviderFactory> factories)
    {
        factories.entrySet()
                 .stream()
                 .forEach(e -> fileSystemProviderFactoryMap.put(extractLayoutAlias(RepositoryFileSystemProviderFactory.class,
                                                                                   e.getKey()),
                                                                e.getValue()));
    }

    @Autowired(required = false)
    public void setFyleSystemFactories(Map<String, RepositoryFileSystemFactory> factories)
    {
        factories.entrySet()
                 .stream()
                 .forEach(e -> fileSystemFactoryMap.put(extractLayoutAlias(RepositoryFileSystemFactory.class,
                                                                           e.getKey()),
                                                        e.getValue()));
    }

    private String extractLayoutAlias(Class<?> factoryClass,
                                      String alias)
    {
        return alias.replace(factoryClass.getSimpleName(), "").substring(1);
    }

    public RepositoryFileSystemFactory lookupRepositoryFileSystemFactory(Repository r)
    {
        return fileSystemFactoryMap.get(r.getLayout());
    }

    public RepositoryFileSystemProviderFactory lookupRepositoryFileSystemProviderFactory(Repository r)
    {
        return fileSystemProviderFactoryMap.get(r.getLayout());
    }
}
