package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.utils.ArtifactControllerHelper;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(path = "/storages")
public abstract class BaseArtifactController
        extends BaseController
{
    @Inject
    protected ArtifactManagementService artifactManagementService;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;

    public Storage getStorage(String storageId)
    {
        return configurationManager.getConfiguration().getStorage(storageId);
    }

    public Repository getRepository(String storageId,
                                    String repositoryId)
    {
        return getStorage(storageId).getRepository(repositoryId);
    }

    //TODO: we need to use `java.nio.file.Path` instead of `String` for `path` parameter here.
    protected boolean provideArtifactDownloadResponse(HttpServletRequest request,
                                                      HttpServletResponse response,
                                                      HttpHeaders httpHeaders,
                                                      RepositoryPath repositoryPath)
            throws Exception
    {
        logger.debug("Resolved path: " + repositoryPath);
        
        ArtifactControllerHelper.provideArtifactHeaders(response, repositoryPath);
        if (response.getStatus() == HttpStatus.NOT_FOUND.value())
        {
            return false;
        }
        else if (request.getMethod().equals(RequestMethod.HEAD.name()))
        {
            return true;
        }

        InputStream is = artifactResolutionService.getInputStream(repositoryPath);
        if (ArtifactControllerHelper.isRangedRequest(httpHeaders))
        {
            logger.debug("Detected ranged request.");

            ArtifactControllerHelper.handlePartialDownload(is, httpHeaders, response);
        }

        artifactEventListenerRegistry.dispatchArtifactDownloadingEvent(repositoryPath);
        copyToResponse(is, response);
        artifactEventListenerRegistry.dispatchArtifactDownloadedEvent(repositoryPath);

        return true;
    }

}
