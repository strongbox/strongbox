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
import javax.ws.rs.core.MediaType;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
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
    @RequestMapping(value = "/{storageId}/{repositoryId}", method = RequestMethod.POST, produces = MediaType.TEXT_XML, consumes = MediaType.TEXT_XML)
    public ResponseEntity<String> searchPackage(@RepositoryMapping Repository repository,
                                                HttpServletRequest request)
        throws IOException,
        Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        logger.info("Request received for package search for storage [{}] , repository [{}]", storageId, repositoryId);

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

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        List<Path> searchResults = Lists.newArrayList();
        for (Object paramValue : paramValues)
        {
            String value = (String) paramValue;

            Paginator paginator = new Paginator();
            Predicate predicate = Predicate.empty();
            predicate.and(Predicate.of(ExpOperator.EQ.of("artifactCoordinates.coordinates.packaging",
                                                         PypiArtifactCoordinates.WHEEL_EXTENSION)));
            predicate.and(Predicate.of(ExpOperator.LIKE.of("artifactCoordinates.coordinates.distribution",
                                                           "%" + value + "%")));

            List<Path> searchResult = repositoryProvider.search(repository.getStorage().getId(), repository.getId(),
                                                                predicate, paginator);

            if (!CollectionUtils.isEmpty(searchResult))
            {
                searchResults.addAll(searchResult);
            }
        }

        return getResponseInXmlRpcFormat(searchResults);
    }

    private ResponseEntity<String> getResponseInXmlRpcFormat(List<Path> paths)
        throws IOException,
        Exception
    {

        logger.info("Found [{}] similar packages for search command.", paths.size());

        List<Object[]> paramsList = new ArrayList<>();
        Object[] paramObject = new Object[paths.size()];
        int idx = 0;
        for (Path path : paths)
        {
            try
            {
                PypiArtifactCoordinates artifactCoordinates = (PypiArtifactCoordinates) RepositoryFiles.readCoordinates((RepositoryPath) path);

                Map<String, Object> params = new HashMap<>();
                params.put("_pypi_ordering", new Boolean(false));
                params.put("version", artifactCoordinates.getVersion());
                params.put("name", artifactCoordinates.getId());
                params.put("summary", "summary"); // TODO :: Add summary fetched from artifact metadata
                paramObject[idx++] = params;

            }
            catch (Exception e)
            {
                logger.error("Failed to read python package path [{}]", path, e);
            }
        }

        paramsList.add(paramObject);

        try (OutputStream outputStream = new ByteArrayOutputStream())
        {
            XmlRpcRequest xmlRpcRequest = new XmlRpcRequestImpl("search", paramsList);

            XMLWriter xmlWriter = new CharSetXMLWriter();
            xmlWriter.setEncoding("UTF-8");
            xmlWriter.setIndenting(false);
            xmlWriter.setFlushing(true);
            xmlWriter.setWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8")));

            XmlRpcStreamRequestConfig xmlRpcStreamRequestConfig = new XmlRpcHttpRequestConfigImpl();
            TypeFactory typeFactory = new TypeFactoryImpl(null);
            XmlRpcWriter xmlRpcWriter = new XmlRpcWriter(xmlRpcStreamRequestConfig, xmlWriter, typeFactory);
            xmlRpcWriter.writeRequest(xmlRpcStreamRequestConfig, xmlRpcRequest);

            byte[] output = ((ByteArrayOutputStream) outputStream).toByteArray();

            return ResponseEntity.status(HttpStatus.OK)
                                 .body(new String(output));
        }

    }
}
