package org.carlspring.strongbox.controllers.layout.pypi;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.web.LayoutRequestMapping;
import org.carlspring.strongbox.web.RepositoryMapping;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import java.net.HttpURLConnection;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This Rest Controller will be used for search/browse python packages
 * 
 * @author ankit.tomar
 *
 */
@RestController
@LayoutRequestMapping(PypiArtifactCoordinates.LAYOUT_NAME)
public class PypiSearchController extends BaseController
{

    @ApiOperation(value = "Used to search an similar packages basis name passed in request")
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Success"),
                            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Requested path not found."),
                            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Server Error"),
                            @ApiResponse(code = HttpURLConnection.HTTP_UNAVAILABLE, message = "Repository currently not in service.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_VIEW')")
    @RequestMapping(value = { "/{storageId}/{repositoryId}/RPC2" }, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML, consumes = MediaType.APPLICATION_XML)
    public void searchPackage(@RepositoryMapping Repository repository,
                              HttpServletRequest request)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        logger.info("Request received for search for package name [{}]", storageId, repositoryId);

    }
}
