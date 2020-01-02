package org.carlspring.strongbox.controllers.layout.pypi;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.web.LayoutRequestMapping;
import org.carlspring.strongbox.web.RepositoryMapping;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

import org.apache.camel.component.xmlrpc.XmlRpcRequestImpl;
import org.apache.camel.dataformat.xmlrpc.XmlRpcDataFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
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
 */
@RestController
@LayoutRequestMapping(PypiArtifactCoordinates.LAYOUT_NAME)
public class PypiSearchController extends BaseController
{

    @ApiOperation(value = "Used to search similar packages basis name or summary passed in request")
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Success"),
                            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Requested path not found."),
                            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Server Error"),
                            @ApiResponse(code = HttpURLConnection.HTTP_UNAVAILABLE, message = "Repository currently not in service.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_VIEW')")
    @RequestMapping(value = { "/{storageId}/{repositoryId}/RPC2" }, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML, consumes = MediaType.APPLICATION_XML)
    public ResponseEntity<String> searchPackage(@RepositoryMapping Repository repository,
                                                HttpServletRequest request)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        logger.info("Request received for search for package name [{}]", storageId, repositoryId);

        XmlRpcDataFormat xmlRpcDataFormat = new XmlRpcDataFormat();
        xmlRpcDataFormat.setRequest(true);

        XmlRpcRequestImpl xmlRpcRequest;
        try
        {
            xmlRpcRequest = (XmlRpcRequestImpl) xmlRpcDataFormat.unmarshal(null,
                                                                           request.getInputStream());
        }
        catch (Exception e)
        {
            logger.error("Something went wrong while parsing xml request..", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String methodName = xmlRpcRequest.getMethodName();
        if (StringUtils.isEmpty(methodName) || !"search".equals(methodName))
        {
            logger.warn("Invalid Method name passed in Request.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Method name passed in Request.");
        }

        if (xmlRpcRequest.getParameterCount() != 2)
        {
            logger.warn("Invalid params count passed in Request.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid params count passed in Request.");
        }

        Map<String, Object[]> param = (Map<String, Object[]>) xmlRpcRequest.getParameter(0);
        Object[] paramValues = param.get("name");

        if (paramValues == null || paramValues.length == 0)
        {
            logger.warn("Invalid param values passed in Request.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid param values passed in Request.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("");
    }
}
