package org.carlspring.strongbox.controllers;

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

import javax.inject.Inject;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
                              "/{storageId}/{repositoryId}/{path}" }, 
                    method = RequestMethod.GET, 
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> contents(@ApiParam(value = "The storageId", required = true)
                                           @PathVariable("storageId") String storageId,
                                           @ApiParam(value = "The repositoryId", required = true)
                                           @PathVariable("repositoryId") String repositoryId,
                                           @ApiParam(value = "The repository path", required = false)
                                           @PathVariable("path") Optional<String> path)
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
            
            Path baseDir = Paths.get(repository.getBasedir());
            if (path.isPresent())
            {
                baseDir = Paths.get(baseDir.toString(), path.get());
            }
            if (!Files.exists(baseDir))
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body("{ 'error': 'The requested repository path was not found.' }");
            }
            
            logger.debug("Listing repository contents for {}/{}: {}", storageId, repositoryId, baseDir.toString());
            
            List<String> directories = new ArrayList<>();
            List<String> files = new ArrayList<>();
            Files.list(baseDir)
                 .filter(p -> !p.getFileName().toString().startsWith("."))
                 .sorted()
                 .forEach(p -> (Files.isDirectory(p) ? directories : files).add(p.getFileName().toString()));
                                             
            Map<String,List<String>> contents = new HashMap<>();
            contents.put("directories", directories);
            contents.put("files", files);
            
            return ResponseEntity.ok(objectMapper.writer().writeValueAsString(contents));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(String.format("{ 'error': '%s' }", e.getMessage()));
        }
    }
    
}
