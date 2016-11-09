package org.carlspring.strongbox.controller;

import java.io.BufferedInputStream;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This Controller used to handle Nuget requests.
 * 
 * @author Sergey Bespalov
 *
 */
@RestController
@RequestMapping("/nuget/storages")
public class NugetPackageController extends BaseController
{

    private static final Logger logger = LogManager.getLogger(NugetPackageController.class.getName());

    @ApiOperation(value = "Used to deploy an package", position = 0)
    @ApiResponses(value =
    { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "The package was deployed successfully."),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    @RequestMapping(value = "{storageId}", method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA)
    public ResponseEntity putPackage(@RequestHeader(name = "X-NuGet-ApiKey", required = false) String apiKey,
                                     @PathVariable(name = "storageId") String storageId,
                                     HttpServletRequest request)
    {
        logger.info(String.format("Nuget push request: storageId-[%s];", storageId));

        if (StringUtils.isEmpty(apiKey))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        URI resourceUri;
        try
        {
            ServletInputStream is = request.getInputStream();
            resourceUri = putPackageInternal(storageId, is);
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.created(resourceUri).build();
    }

    private URI putPackageInternal(String storageId,
                                   InputStream is)
            throws IOException,
            URISyntaxException,
            NoSuchAlgorithmException
    {

        int readLength;
        int total = 0;
        byte[] bytes = new byte[1024];
        try (BufferedInputStream bis = new BufferedInputStream(is);)
        {
            while ((readLength = bis.read(bytes)) != -1)
            {
                total += readLength;
            }
        }

        logger.debug(String.format("Nuget push request content: storageId-[%s]; readLength-[%s]", storageId, total));

        return new URI("");
    }

}
