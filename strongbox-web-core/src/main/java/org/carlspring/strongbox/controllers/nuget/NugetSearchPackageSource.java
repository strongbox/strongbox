package org.carlspring.strongbox.controllers.nuget;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.PathNupkg;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.repository.RepositoryPageRequest;
import org.carlspring.strongbox.providers.repository.RepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.providers.repository.RepositorySearchRequest;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import ru.aristar.jnuget.Version;
import ru.aristar.jnuget.files.NugetFormatException;
import ru.aristar.jnuget.files.Nupkg;
import ru.aristar.jnuget.sources.AbstractPackageSource;

@Component
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class NugetSearchPackageSource extends AbstractPackageSource<Nupkg>
{

    private static final Logger logger = LoggerFactory.getLogger(NugetSearchPackageSource.class);

    private String searchTerm;

    private String storageId;

    private String repositoryId;
    
    private Integer skip;
    
    private Integer top;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;

    private String orderBy = "id";

    public NugetSearchPackageSource(String storageId,
                                    String repositoryId,
                                    String searchTerm)
    {
        setSearchTerm(searchTerm);
        setStorageId(storageId);
        setRepositoryId(repositoryId);
    }

    public NugetSearchPackageSource()
    {
        super();
    }

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public String getSearchTerm()
    {
        return searchTerm == null ? "" : searchTerm;
    }

    public void setSearchTerm(String searchTerm)
    {
        this.searchTerm = searchTerm == null ? null : getSearchTerm().replaceAll("'", "");
    }

    public Integer getSkip()
    {
        return skip;
    }

    public void setSkip(Integer skip)
    {
        this.skip = skip;
    }

    public Integer getTop()
    {
        return top;
    }

    public void setTop(Integer top)
    {
        this.top = top;
    }

    public void setOrderBy(String orderBy)
    {
        this.orderBy = orderBy;
    }

    public String getOrderBy()
    {
        return orderBy;
    }

    @Override
    public Collection<Nupkg> getPackages()
    {
        Map<String, String> coordinates = new HashMap<>();
        coordinates.put("extension", "nupkg");
        if (searchTerm != null && !searchTerm.trim().isEmpty())
        {
            coordinates.put("id", searchTerm);
        }

        return doSearch(coordinates, false);
    }

    public List<Nupkg> createPackageList(List<Path> artifactPathList)
    {
        List<Nupkg> result = new ArrayList<>();
        for (Path artifactPath : artifactPathList)
        {
            Nupkg nupkg = createPackage((RepositoryPath) artifactPath);
            if (nupkg == null)
            {
                continue;
            }

            result.add(nupkg);
        }
        return result;
    }

    public Nupkg createPackage(RepositoryPath repositoryPath)
    {
        try
        {
            return new PathNupkg(repositoryPath);
        }
        catch (NugetFormatException | IOException e)
        {
            throw new RuntimeException(String.format("Failed to create Nupkg file for [%s]", repositoryPath.toString()),
                    e);
        }
    }

    @Override
    public Collection<Nupkg> getLastVersionPackages()
    {
        // TODO: implement Latest Version Package search
        return getPackages();
    }

    @Override
    public Collection<Nupkg> getPackages(String id)
    {
        Map<String, String> coordinates = new HashMap<>();
        coordinates.put("extension", "nupkg");
        coordinates.put("id", id);

        return doSearch(coordinates, true);
    }

    @Override
    public Nupkg getLastVersionPackage(String id)
    {
        // TODO: implement Latest Version Package search
        Collection<Nupkg> packageList = getPackages(id);
        return packageList.isEmpty() ? null : packageList.iterator().next();
    }

    @Override
    public Nupkg getPackage(String id,
                            Version version)
    {
        Map<String, String> coordinates = new HashMap<>();
        coordinates.put("extension", "nupkg");
        coordinates.put("id", id);
        coordinates.put("version", version.toString());

        List<Nupkg> packageList = doSearch(coordinates, true);

        return packageList.isEmpty() ? null : packageList.iterator().next();
    }

    private List<Nupkg> doSearch(Map<String, String> coordinates,
                                 boolean strict)
    {
        Storage storage = layoutProviderRegistry.getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        RepositorySearchRequest searchRequest = new RepositorySearchRequest(storageId, repositoryId);
        searchRequest.setStrict(strict);
        searchRequest.setCoordinates(coordinates);
        
        RepositoryPageRequest pageRequest = new RepositoryPageRequest();
        pageRequest.setSkip(skip);
        pageRequest.setLimit(top);

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());
        List<Path> searchResult = repositoryProvider.search(searchRequest, pageRequest);

        List<Nupkg> packageList = createPackageList(searchResult);
        return packageList;
    }

    @Override
    public void removePackage(Nupkg nupkg)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshPackage(Nupkg nupkg)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void processPushPackage(Nupkg nupkg)
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

}