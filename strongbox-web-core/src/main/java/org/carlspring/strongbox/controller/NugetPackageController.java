package org.carlspring.strongbox.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This Controller used to handle Nuget requests.
 * 
 * @author Sergey Bespalov
 *
 */
@RestController
@RequestMapping(path = NugetPackageController.ROOT_CONTEXT, headers = "user-agent=NuGet/*")
public class NugetPackageController extends BaseArtifactController
{

    private static final Logger logger = LogManager.getLogger(NugetPackageController.class.getName());

    public final static String ROOT_CONTEXT = "/storages";

    @ApiOperation(value = "Used to deploy an package", position = 0)
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "The package was deployed successfully."),
                            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    @RequestMapping(value = "{storageId}/{repositoryId}/**", method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA)
    public ResponseEntity putPackage(
                                     @RequestHeader(name = "X-NuGet-ApiKey", required = false)
                                     String apiKey,
                                     @ApiParam(value = "The storageId", required = true)
                                     @PathVariable(name = "storageId")
                                     String storageId,
                                     @ApiParam(value = "The repositoryId", required = true)
                                     @PathVariable(name = "repositoryId")
                                     String repositoryId,
                                     HttpServletRequest request)
    {
        logger.info(String.format("Nuget push request: storageId-[%s]; repositoryId-[%s]", storageId, repositoryId));

        if (StringUtils.isEmpty(apiKey))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String contextPath = request.getContextPath();
        String path = request.getRequestURI().replaceAll(contextPath, "").replaceAll(ROOT_CONTEXT, "");

        URI resourceUri;
        try
        {
            ServletInputStream is = request.getInputStream();
            resourceUri = putPackageInternal(storageId, repositoryId, path, is);
        }
        catch (Exception e)
        {
            logger.error(String.format("Failed to process Nuget push request: storageId-[%s]; repositoryId-[%s]",
                                       storageId, repositoryId),
                         e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.created(resourceUri).build();
    }

    private URI putPackageInternal(
                                   String storageId,
                                   String repositoryId,
                                   String path,
                                   InputStream is) throws IOException, URISyntaxException, NoSuchAlgorithmException, ProviderImplementationException
    {

        getArtifactManagementService().store(storageId, repositoryId, path, is);

        // logger.debug(String.format("Nuget push request content:
        // storageId-[%s]; readLength-[%s]", storageId, total));

        return new URI("");
    }

}
