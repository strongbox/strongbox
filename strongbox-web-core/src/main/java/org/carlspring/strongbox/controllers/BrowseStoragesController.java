package org.carlspring.strongbox.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.carlspring.strongbox.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sanket407
 */
@RestController
@RequestMapping(path = BrowseStoragesController.ROOT_CONTEXT, headers = "user-agent=unknown/*")
public class BrowseStoragesController
        extends BaseArtifactController
{
    
    private Logger logger = LoggerFactory.getLogger(BrowseStoragesController.class);

    public static final String ROOT_CONTEXT = "/storages";


    @ApiOperation(value = "Used to browse the configured storages")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = { "" , "/" }, method = RequestMethod.GET)
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
    @RequestMapping(value = { "{storageId}/" , "{storageId}" }, method = RequestMethod.GET)
    public void browseRepositores(@ApiParam(value = "The storageId", required = true)
                                  @PathVariable String storageId,
                                  HttpServletRequest request,
                                  HttpServletResponse response) 
            throws IOException
    {
        logger.debug("Requested browsing for repositores in storage : " + storageId);

        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        if (storage == null)
        {
            logger.error("Unable to find storage by ID " + storageId);

            response.sendError(HttpStatus.NOT_FOUND.value(), "Unable to find storage by ID " + storageId);

            return;
        }

        getDirectoryListing(storage, request, response);
    }
}
