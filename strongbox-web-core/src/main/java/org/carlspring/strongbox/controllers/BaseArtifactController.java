package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.utils.ArtifactControllerHelper;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import io.swagger.annotations.Api;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(value = "/storages")
public abstract class BaseArtifactController
        extends BaseController
{

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private StorageProviderRegistry storageProviderRegistry;

    @Inject
    protected ArtifactManagementService artifactManagementService;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;
    
    // ----------------------------------------------------------------------------------------------------------------
    // Common-purpose methods

    public Storage getStorage(String storageId)
    {
        return configurationManager.getConfiguration()
                                   .getStorage(storageId);
    }

    public Repository getRepository(String storageId,
                                    String repositoryId)
    {
        return getStorage(storageId).getRepository(repositoryId);
    }

    public LayoutProviderRegistry getLayoutProviderRegistry()
    {
        return layoutProviderRegistry;
    }

    public void setLayoutProviderRegistry(LayoutProviderRegistry layoutProviderRegistry)
    {
        this.layoutProviderRegistry = layoutProviderRegistry;
    }

    public StorageProviderRegistry getStorageProviderRegistry()
    {
        return storageProviderRegistry;
    }

    public void setStorageProviderRegistry(StorageProviderRegistry storageProviderRegistry)
    {
        this.storageProviderRegistry = storageProviderRegistry;
    }
    
    protected boolean provideArtifactDownloadResponse(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   HttpHeaders httpHeaders,
                                                   Repository repository,
                                                   String path)
        throws IOException,
        ArtifactTransportException,
        ProviderImplementationException,
        Exception
    {
        String storageId = repository.getStorage().getId();
        String repositoryId = repository.getId();
        
        RepositoryPath resolvedPath = artifactManagementService.getPath(storageId, repositoryId, path);
        logger.debug("Resolved path : " + resolvedPath);
        
        ArtifactControllerHelper.provideArtifactHeaders(response, resolvedPath);
        if (response.getStatus() == HttpStatus.NOT_FOUND.value())
        {
            return false;
        }
        else if (request.getMethod().equals(RequestMethod.HEAD.name()))
        {
            return true;
        }

        logger.debug("Proceeding downloading : " + resolvedPath);
        InputStream is = artifactManagementService.resolve(storageId, repositoryId, path);
        if (ArtifactControllerHelper.isRangedRequest(httpHeaders))
        {
            logger.debug("Detecting range request....");
            ArtifactControllerHelper.handlePartialDownload(is, httpHeaders, response);
        }

        artifactEventListenerRegistry.dispatchArtifactDownloadingEvent(storageId, repositoryId, path);
        copyToResponse(is, response);
        artifactEventListenerRegistry.dispatchArtifactDownloadedEvent(storageId, repositoryId, path);

        logger.debug("Download succeeded.");
        
        return true;
    }

}
