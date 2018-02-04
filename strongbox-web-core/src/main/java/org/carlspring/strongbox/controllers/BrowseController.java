package org.carlspring.strongbox.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.carlspring.strongbox.artifact.coordinates.AbstractArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.domain.DirectoryContent;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST API for browsing storage/repository/filesystem structures.
 *
 * @author Guido Grazioli <guido.grazioli@gmail.com>
 */
@RestController
@RequestMapping(path = BrowseController.ROOT_CONTEXT)
public class BrowseController
        extends BaseArtifactController
{

    private static final Logger logger = LoggerFactory.getLogger(BrowseController.class);

    // must be the same as @RequestMapping value on the class definition
    public final static String ROOT_CONTEXT = "/browse";
    
    @Inject
    private ObjectMapper objectMapper;
    
    @ApiOperation(value = "List configured storages.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The list was returned."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> browse()
    {
        try
        {
            Map<String,SortedSet<String>> storages = new HashMap<>();
            storages.put("storages", new TreeSet<String>(configurationManager.getConfiguration()
                    .getStorages()
                    .keySet()));
            
            return ResponseEntity.ok(objectMapper.writer().writeValueAsString(storages));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(String.format("{ 'error': '%s' }", e.getMessage()));
        }
    }

    @ApiOperation(value = "List configured repositories for a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The list was returned."),
                            @ApiResponse(code = 404, message = "The requested storage was not found."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value="/{storageId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> repositories(@ApiParam(value = "The storageId", required = true)
                                               @PathVariable("storageId") String storageId)
    {
        try
        {
            Storage storage = configurationManager.getConfiguration().getStorage(storageId);
            if (storage == null) 
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body("{ 'error': 'The requested storage was not found.' }");
            }
            
            Map<String,SortedSet<String>> repos = new HashMap<>();
            repos.put("repositories", new TreeSet<String>(storage.getRepositories().keySet()));
            
            return ResponseEntity.ok(objectMapper.writer().writeValueAsString(repos));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(String.format("{ 'error': '%s' }", e.getMessage()));
        }
    }
    
    @ApiOperation(value = "List the contents for a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The list was returned."),
                            @ApiResponse(code = 404, message = "The requested storage, repository, or path was not found."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = { "/{storageId}/{repositoryId}",
                              "/{storageId}/{repositoryId}/**" }, 
                    method = RequestMethod.GET, 
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> contents(@ApiParam(value = "The storageId", required = true)
                                           @PathVariable("storageId") String storageId,
                                           @ApiParam(value = "The repositoryId", required = true)
                                           @PathVariable("repositoryId") String repositoryId,
                                           @ApiParam(value = "The repository path", required = false)
                                           @PathVariable("path") Optional<String> path,
                                           HttpServletRequest request)
    {
        try
        {
            Storage storage = configurationManager.getConfiguration().getStorage(storageId);
            if (storage == null) 
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body("{ 'error': 'The requested storage was not found.' }");
            }
            
            Repository repository = storage.getRepository(repositoryId);
            if (repository == null) 
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body("{ 'error': 'The requested repository was not found.' }");
            }
            
            String matchedPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String subPath = new AntPathMatcher().extractPathWithinPattern(matchedPattern, request.getRequestURI());
            Path dirPath = Paths.get(repository.getBasedir().toString(), subPath);
            if (!Files.exists(dirPath))
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body("{ 'error': 'The requested repository path was not found.' }");
            }

            logger.debug("Listing repository contents for {}/{}: {}", storageId, repositoryId, dirPath.toString());
            
            DirectoryContent directory = new DirectoryContent(dirPath);
            
            Map<String, List<File>> contents = directory.getContents();
            
            Map<String, List<String>> names = new HashMap<String, List<String>>();
            
            names.put("directories", contents.get("directories").stream()
                                                                .map(File::getName)
                                                                .collect(Collectors.toList()));
            names.put("files", contents.get("files").stream()
                                                    .map(File::getName)
                                                    .collect(Collectors.toList()));
            
            return ResponseEntity.ok(objectMapper.writer().writeValueAsString(names));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(String.format("{ 'error': '%s' }", e.getMessage()));
        }
    }
    
    @ApiOperation(value = "Used to browse the configured storages")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(method = RequestMethod.GET, 
                    produces = MediaType.TEXT_HTML_VALUE)
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
    @RequestMapping(value = { "/{storageId}" }, 
                    method = RequestMethod.GET,
                    produces = MediaType.TEXT_HTML_VALUE)
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
    
    @ApiOperation(value = "List the contents for a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The list was returned."),
                            @ApiResponse(code = 404, message = "The requested storage, repository, or path was not found."),
                            @ApiResponse(code = 500, message = "An error occurred."),
                            @ApiResponse(code = 503, message = "Repository not in service")})
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = { "/{storageId}/{repositoryId}",
                              "/{storageId}/{repositoryId}/**" }, 
                    method = RequestMethod.GET, 
                    produces = MediaType.TEXT_HTML_VALUE)
    public void browseRepositoryContents(@ApiParam(value = "The storageId", required = true)
                                         @PathVariable("storageId") String storageId,
                                         @ApiParam(value = "The repositoryId", required = true)
                                         @PathVariable("repositoryId") String repositoryId,
                                         @ApiParam(value = "The repository path", required = false)
                                         @PathVariable("path") Optional<String> path,
                                         HttpServletRequest request,
                                         HttpServletResponse response)
                throws Exception
    {   
        
        
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
        
        String matchedPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String subPath = new AntPathMatcher().extractPathWithinPattern(matchedPattern, request.getRequestURI());
        
        logger.debug("Requested browsing for {}/{}/{} ", storageId, repositoryId, subPath);
        
        if (repository.allowsDirectoryBrowsing() && probeForDirectoryListing(repository, subPath))
        {
            try
            {
                getDirectoryListing(repository, subPath, request, response);
            }
            catch (Exception e)
            {
                logger.debug("Unable to generate directory listing for " +
                        "/" + storageId + "/" + repositoryId + "/" + path, e);

                response.setStatus(INTERNAL_SERVER_ERROR.value());
            }

            return;
        }
        return;
    }    
}
