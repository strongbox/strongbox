package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.utils.ArtifactControllerHelper;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;

public abstract class BaseArtifactController
        extends BaseController
{

    @Inject
    protected ArtifactManagementService artifactManagementService;

    protected boolean provideArtifactDownloadResponse(HttpServletRequest request,
                                                      HttpServletResponse response,
                                                      HttpHeaders httpHeaders,
                                                      RepositoryPath repositoryPath)
            throws Exception
    {
        logger.debug("Resolved path: {}", repositoryPath);
        ArtifactControllerHelper.provideArtifactHeaders(response, repositoryPath);
        if (response.getStatus() == HttpStatus.NOT_FOUND.value())
        {
            return false;
        }
        else if (request.getMethod().equals(RequestMethod.HEAD.name()))
        {
            return true;
        }

        
        try (InputStream is = artifactResolutionService.getInputStream(repositoryPath);)
        {
            if (ArtifactControllerHelper.isRangedRequest(httpHeaders))
            {
                logger.debug("Detected ranged request.");

                ArtifactControllerHelper.handlePartialDownload(is, httpHeaders, response);
            }
            
            copyToResponse(is, response);
        }

        return true;
    }

    protected ResponseEntity provideArtifactUploading(Repository repository, String filePath, InputStream is)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        try
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, filePath);
            artifactManagementService.validateAndStore(repositoryPath, is);

            return ResponseEntity.ok("The artifact was deployed successfully.");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
