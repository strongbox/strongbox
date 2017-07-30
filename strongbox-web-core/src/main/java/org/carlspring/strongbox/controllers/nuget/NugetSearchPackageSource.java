package org.carlspring.strongbox.controllers.nuget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.PathNupkg;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import ru.aristar.jnuget.Version;
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
    
    @Inject
    private ArtifactEntryService artifactEntryService;
    
    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    private String orderBy = "id";
    
    public NugetSearchPackageSource(String storageId,
                                    String repositoryId,
                                    String searchTerm)
    {
        setSearchTerm(searchTerm);;
        setStorageId(storageId);
        setRepositoryId(repositoryId);
    }

    public NugetSearchPackageSource()
    {
        super();
    }

    protected String getStorageId()
    {
        return storageId;
    }

    protected void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    protected String getRepositoryId()
    {
        return repositoryId;
    }

    protected void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    protected String getSearchTerm()
    {
        return searchTerm == null ? "" : searchTerm;
    }

    protected void setSearchTerm(String searchTerm)
    {
        this.searchTerm = searchTerm == null ? null : getSearchTerm().replaceAll("'", "");
    }

    public void setOrderBy(String orderBy)
    {
        if (!orderBy.equals("id") || !orderBy.equals("version")){
            return;
        }
        this.orderBy = orderBy;
    }
    
    protected String getOrderBy()
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
            coordinates.put("id", searchTerm + "%");
        }
        
        List<ArtifactEntry> searchResultList = artifactEntryService.findByCoordinates(coordinates, orderBy, false);
        
        return createPackageList(searchResultList);
    }

    public List<Nupkg> createPackageList(List<ArtifactEntry> searchResultList)
    {
        List<Nupkg> result = new ArrayList<>();
        for (ArtifactEntry searchResult : searchResultList)
        {
            Nupkg nupkg = createPackage(searchResult);
            if (nupkg == null)
            {
                continue;
            }

            result.add(nupkg);
        }
        return result;
    }

    public Nupkg createPackage(ArtifactEntry searchResult)
    {
        ArtifactCoordinates artifactCoordinates = searchResult.getArtifactCoordinates();
        Storage storage = layoutProviderRegistry.getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        LayoutProvider provider = layoutProviderRegistry.getProvider(repository.getLayout());
        try
        {
            return new PathNupkg(provider.resolve(repository, artifactCoordinates));
        }
        catch (IOException e)
        {
            logger.error(String.format("Failed to resolve package RepositoryPath for [%s]",
                                       artifactCoordinates.toPath()));
        }
        return null;
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
        
        List<ArtifactEntry> searchResultList = artifactEntryService.findByCoordinates(coordinates, orderBy, true);
        
        return createPackageList(searchResultList);
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
        
        List<ArtifactEntry> searchResultList = artifactEntryService.findByCoordinates(coordinates);
        List<Nupkg> result = createPackageList(searchResultList);
        
        return result.isEmpty() ? null : result.iterator().next();
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