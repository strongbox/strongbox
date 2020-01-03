package org.carlspring.strongbox.controllers.layout.pypi;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.criteria.Paginator;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.repository.RepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.web.LayoutRequestMapping;
import org.carlspring.strongbox.web.RepositoryMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.component.xmlrpc.XmlRpcRequestImpl;
import org.apache.camel.dataformat.xmlrpc.XmlRpcDataFormat;
import org.apache.camel.dataformat.xmlrpc.XmlRpcWriter;
import org.apache.ws.commons.serialize.CharSetXMLWriter;
import org.apache.ws.commons.serialize.XMLWriter;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.TypeFactory;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfigImpl;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
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

    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @ApiOperation(value = "Used to search similar packages basis name or summary passed in request")
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Success"),
                            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Requested path not found."),
                            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Server Error"),
                            @ApiResponse(code = HttpURLConnection.HTTP_UNAVAILABLE, message = "Repository currently not in service.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_VIEW')")
    @RequestMapping(value = { "/{storageId}/{repositoryId}/RPC2" }, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML, consumes = MediaType.APPLICATION_XML)
    public void searchPackage(@RepositoryMapping Repository repository,
                                                HttpServletRequest request,
                                                HttpServletResponse response)
        throws IOException,
        Exception
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
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return;
        }

        String methodName = xmlRpcRequest.getMethodName();
        if (StringUtils.isEmpty(methodName) || !"search".equals(methodName))
        {
            logger.warn("Invalid Method name passed in Request.");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Method name passed in Request.");
            response.setStatus(HttpStatus.BAD_REQUEST.value(),"Invalid Method name passed in Request.");
            return;
        }

        if (xmlRpcRequest.getParameterCount() != 2)
        {
            logger.warn("Invalid params count passed in Request.");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid params count passed in Request.");
            response.setStatus(HttpStatus.BAD_REQUEST.value(),"Invalid params count passed in Request.");
            return;
        }

        Map<String, Object[]> param = (Map<String, Object[]>) xmlRpcRequest.getParameter(0);
        Object[] paramValues = param.get("name");

        if (paramValues == null || paramValues.length == 0)
        {
            logger.warn("Invalid param values passed in Request.");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid param values passed in Request.");
            response.setStatus(HttpStatus.BAD_REQUEST.value(),"Invalid params count passed in Request.");
            return;
        }

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        Predicate predicate = Predicate.empty();
        predicate.and(Predicate.of(ExpOperator.EQ.of("artifactCoordinates.coordinates.packaging",
                                                     PypiArtifactCoordinates.WHEEL_EXTENSION)));

        Predicate orPredicate = Predicate.empty();
        for (Object paramValue : paramValues)
        {
            String value = (String) paramValue;
            orPredicate.or(Predicate.of(ExpOperator.LIKE.of("artifactCoordinates.coordinates.distribution",
                                                            value)));
        }

        predicate.and(orPredicate);

        Paginator paginator = new Paginator();
        List<Path> searchResult = repositoryProvider.search(repository.getStorage().getId(), repository.getId(),
                                                            predicate, paginator);

        populateXmlResponse(searchResult, response);
    }

    private void populateXmlResponse(List<Path> searchResult,
                                                  HttpServletResponse response)
        throws IOException,
        Exception
    {

        List<Object> paramsList = new ArrayList<>();
        searchResult.stream().forEach(path -> {
            try
            {
                PypiArtifactCoordinates artifactCoordinates = (PypiArtifactCoordinates) RepositoryFiles.readCoordinates((RepositoryPath) path);

                Map<String, Object> params = new HashMap<>();
                params.put("_pypi_ordering", new Integer(0));
                params.put("version", artifactCoordinates.getVersion());
                params.put("name", artifactCoordinates.getId());
                params.put("summary", artifactCoordinates.getCoordinates().get("summary"));

                paramsList.add(params);
            }
            catch (Exception e)
            {
                logger.error("Failed to read python package path [{}]", path, e);
            }
        });

        XmlRpcRequest xmlRpcRequest = new XmlRpcRequestImpl("search", paramsList);

        XMLWriter xmlWriter = new CharSetXMLWriter();
        xmlWriter.setEncoding("UTF-8");
        xmlWriter.setIndenting(false);
        xmlWriter.setFlushing(true);
        xmlWriter.setWriter(new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8")));

        XmlRpcStreamRequestConfig xmlRpcStreamRequestConfig = new XmlRpcHttpRequestConfigImpl();
        TypeFactory typeFactory = new TypeFactoryImpl(null);
        XmlRpcWriter xmlRpcWriter = new XmlRpcWriter(xmlRpcStreamRequestConfig, xmlWriter, typeFactory);
        xmlRpcWriter.write(xmlRpcRequest);

    }
}
