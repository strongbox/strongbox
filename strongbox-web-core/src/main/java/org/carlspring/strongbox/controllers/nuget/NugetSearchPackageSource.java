package org.carlspring.strongbox.controllers.nuget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.PathNupkg;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;
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
    private ArtifactSearchService artifactSearchService;
    
//    @Inject
//    private ArtifactResolutionService artifactResolutionService;
    
    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;
    
    public NugetSearchPackageSource(String storageId,
                                    String repositoryId,
                                    String searchTerm)
    {
        this.searchTerm = searchTerm;
        this.storageId = storageId;
        this.repositoryId = repositoryId;
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
        return searchTerm;
    }

    protected void setSearchTerm(String searchTerm)
    {
        this.searchTerm = searchTerm;
    }

    @Override
    public Collection<Nupkg> getPackages()
    {
        ArrayList<Nupkg> result = new ArrayList<>();
        
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(String.format("id=%s;extension=nupkg;", getSearchTerm().replaceAll("'", "")));
        SearchResults searchResults;
        try
        {
            searchResults = artifactSearchService.search(searchRequest);
        }
        catch (SearchException e)
        {
            logger.error(String.format("Failed to search packages within [%s]/[%s]", storageId, repositoryId), e);
            return result;
        }
        
        Set<SearchResult> searchResultSet = searchResults.getResults();
        for (SearchResult searchResult : searchResultSet)
        {
            ArtifactCoordinates artifactCoordinates = searchResult.getArtifactCoordinates();
            Storage storage = layoutProviderRegistry.getStorage(storageId);
            Repository repository = storage.getRepository(repositoryId);
            LayoutProvider provider = layoutProviderRegistry.getProvider(repository.getLayout());
            try
            {
                result.add(new PathNupkg(provider.resolve(repository, artifactCoordinates)));
            }
            catch (IOException e)
            {
                logger.error(String.format("Failed to resolve package RepositoryPath for [%s]",
                                           artifactCoordinates.toPath()));
            }
        }
        return result;
    }

    @Override
    public Collection<Nupkg> getLastVersionPackages()
    {
        // TODO: implement Package search
        return getPackages();
    }

    @Override
    public Collection<Nupkg> getPackages(String id)
    {
        // TODO: implement Package search
        return getPackages();
    }

    @Override
    public Nupkg getLastVersionPackage(String id)
    {
        // TODO: implement Package search
        return null;
    }

    @Override
    public Nupkg getPackage(String id,
                            Version version)
    {
        // TODO: implement Package search
        return null;
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