package org.carlspring.strongbox.controllers;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sanket407
 */
@RestController
@RequestMapping(path = BrowseStoragesController.ROOT_CONTEXT,
                produces = MediaType.TEXT_HTML_VALUE)
public class BrowseStoragesController
        extends BaseArtifactController
{
    
    private Logger logger = LoggerFactory.getLogger(BrowseStoragesController.class);

    public static final String ROOT_CONTEXT = "/storages";


    @ApiOperation(value = "Used to browse the configured storages")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = { "" , "/" }, method = RequestMethod.GET, headers = "user-agent=unknown/*")
    public void browseStorages(HttpServletRequest request,
                               HttpServletResponse response)
    {
        logger.debug("Requested browsing for storages");

        getDirectoryListing(request, response);
    }

    @ApiOperation(value = "Used to browse the repositories in a storage")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = { "{storageId}/" , "{storageId}" }, method = RequestMethod.GET, headers = "user-agent=unknown/*")
    public void browseRepositories(@ApiParam(value = "The storageId", required = true)
                                   @PathVariable String storageId,
                                   HttpServletRequest request,
                                   HttpServletResponse response)
            throws IOException
    {
        logger.debug("Requested browsing for repositories in storage : " + storageId);

        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        if (storage == null)
        {
            logger.error("Unable to find storage by ID " + storageId);

            response.sendError(HttpStatus.NOT_FOUND.value(), "Unable to find storage by ID " + storageId);

            return;
        }

        getDirectoryListing(storage, request, response);
    }
    
    @ApiOperation(value = "Used to browse inside repositories", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 404, message = "Requested path not found."),
                            @ApiResponse(code = 500, message = "Server Error."),
                            @ApiResponse(code = 503, message = "Repository not in service currently.")})
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = { "{storageId}/{repositoryId}/{path:.+}" }, method = {RequestMethod.GET}, headers = {"user-agent=Maven/*"})
    public void browseMavenRepositoriesInternal(@ApiParam(value = "The storageId", required = true)
                                                @PathVariable String storageId,
                                                @ApiParam(value = "The repositoryId", required = true)
                                                @PathVariable String repositoryId,
                                                @RequestHeader HttpHeaders httpHeaders,
                                                @PathVariable String path,
                                                HttpServletRequest request,
                                                HttpServletResponse response)
            throws Exception
    {   
        
        browse(storageId, repositoryId, path, request, response, httpHeaders);
    }
    
    @ApiOperation(value = "Used to browse inside repositories", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 404, message = "Requested path not found."),
                            @ApiResponse(code = 500, message = "Server Error."),
                            @ApiResponse(code = 503, message = "Repository not in service currently.")})
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = { "{storageId}/{repositoryId}/{path:.+}" }, method = {RequestMethod.GET}, headers = {"user-agent=NuGet/*"})
    public void browseNugetRepositoriesInternal(@ApiParam(value = "The storageId", required = true)
                                                @PathVariable String storageId,
                                                @ApiParam(value = "The repositoryId", required = true)
                                                @PathVariable String repositoryId,
                                                @RequestHeader HttpHeaders httpHeaders,
                                                @PathVariable String path,
                                                HttpServletRequest request,
                                                HttpServletResponse response)
            throws Exception
    {   
        
        browse(storageId, repositoryId, path, request, response, httpHeaders);
    }
    
    @ApiOperation(value = "Used to browse inside repositories", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 404, message = "Requested path not found."),
                            @ApiResponse(code = 500, message = "Server Error."),
                            @ApiResponse(code = 503, message = "Repository not in service currently.")})
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = { "{storageId}/{repositoryId}/{path:.+}" }, method = {RequestMethod.GET}, headers = {"user-agent=npm/*"})
    public void browseNpmRepositoriesInternal(@ApiParam(value = "The storageId", required = true)
                                              @PathVariable String storageId,
                                              @ApiParam(value = "The repositoryId", required = true)
                                              @PathVariable String repositoryId,
                                              @RequestHeader HttpHeaders httpHeaders,
                                              @PathVariable String path,
                                              HttpServletRequest request,
                                              HttpServletResponse response)
            throws Exception
    {   
        
        browse(storageId, repositoryId, path, request, response, httpHeaders);
    }
    
    @ApiOperation(value = "Used to browse inside repositories", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 404, message = "Requested path not found."),
                            @ApiResponse(code = 500, message = "Server Error."),
                            @ApiResponse(code = 503, message = "Repository not in service currently.")})
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = { "{storageId}/{repositoryId}/{path:.+}" }, method = {RequestMethod.GET}, headers = {"user-agent=Raw/*"})
    public void browseRawRepositoriesInternal(@ApiParam(value = "The storageId", required = true)
                                              @PathVariable String storageId,
                                              @ApiParam(value = "The repositoryId", required = true)
                                              @PathVariable String repositoryId,
                                              @RequestHeader HttpHeaders httpHeaders,
                                              @PathVariable String path,
                                              HttpServletRequest request,
                                              HttpServletResponse response)
            throws Exception
    {   
        
        browse(storageId, repositoryId, path, request, response, httpHeaders);
    }
       
    public void browse(String storageId,
                       String repositoryId,
                       String path,
                       HttpServletRequest request,
                       HttpServletResponse response,
                       HttpHeaders httpHeaders) 
            throws Exception
    {
        logger.debug("Requested browsing for /" + storageId + "/" + repositoryId + "/" + path + ".");

        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        if (storage == null)
        {
            logger.error("Unable to find storage by ID " + storageId);

            response.sendError(INTERNAL_SERVER_ERROR.value(), "Unable to find storage by ID " + storageId);

            return;
        }

        Repository repository = storage.getRepository(repositoryId);
        if (repository == null)
        {
            logger.error("Unable to find repository by ID " + repositoryId + " for storage " + storageId);

            response.sendError(INTERNAL_SERVER_ERROR.value(),
                               "Unable to find repository by ID " + repositoryId + " for storage " + storageId);
            return;
        }

        if (!repository.isInService())
        {
            logger.error("Repository is not in service...");

            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());

            return;
        }

        if (repository.allowsDirectoryBrowsing() && probeForDirectoryListing(repository, path))
        {
            try
            {
                getDirectoryListing(repository, path, request, response);
            }
            catch (Exception e)
            {
                logger.debug("Unable to generate directory listing for " +
                        "/" + storageId + "/" + repositoryId + "/" + path, e);

                response.setStatus(INTERNAL_SERVER_ERROR.value());
            }

            return;
        }
        
        logger.debug("Requested path not a directory");
        logger.debug(request.getHeader("user-agent"));
      
        if(request.getHeader("user-agent").equals("NuGet/*"))
        {   
            String parts[] = path.split("/");
            String packageId = parts[parts.length-2];
            String packageVersion = parts[parts.length-1];
            String fileName = String.format("%s.%s.nupkg", packageId, packageVersion);
            path = String.format("%s/%s/%s", packageId, packageVersion, fileName);
        }
        
        provideArtifactDownloadResponse(request, response, httpHeaders, repository, path);
        
    }
}
