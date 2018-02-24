package org.carlspring.strongbox.controllers;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.carlspring.strongbox.domain.DirectoryContent;
import org.carlspring.strongbox.domain.FileContent;
import org.carlspring.strongbox.providers.repository.RepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.services.DirectoryContentFetcher;
import org.carlspring.strongbox.services.impl.ArtifactResolutionServiceImpl;
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
    private DirectoryContentFetcher directoryContentFetcher;
    
    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private ArtifactResolutionService artifactResolutionService;
    
    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;
    
    @ApiOperation(value = "List configured storages.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The list was returned."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> browse()
    {
        try
        {               
            Set<Entry<String, Storage>> storages = configurationManager.getConfiguration().getStorages().entrySet();
                                                    
            List<Path> storagePaths = new ArrayList<Path>();
            
            for(Entry<String, Storage> entry : storages)
            {
                Storage storage = entry.getValue();
                String storageBaseDir = storage.getBasedir();
                storagePaths.add(Paths.get(storageBaseDir));
            }
            
            DirectoryContent content = directoryContentFetcher.fetchDirectoryContent(storagePaths);           
            
            return ResponseEntity.ok(objectMapper.writer().writeValueAsString(content));
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
            
            Set<Entry<String, Repository>> repositories = storage.getRepositories().entrySet();
            
            List<Path> repositoryPaths = new ArrayList<Path>();
            
            for(Entry<String, Repository> entry : repositories)
            {
                Repository repository = entry.getValue();
                RepositoryProvider respositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());
                Path repositoryBaseDir = respositoryProvider.resolvePath(storageId, repository.getId(), "");
                repositoryPaths.add(repositoryBaseDir);
            }
                        
            DirectoryContent content = directoryContentFetcher.fetchDirectoryContent(repositoryPaths);            
            
            return ResponseEntity.ok(objectMapper.writer().writeValueAsString(content));
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
    @RequestMapping(value = { "{storageId}/{repositoryId}/{path:.+}" },
                    method = RequestMethod.GET, 
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> contents(@ApiParam(value = "The storageId", required = true)
                                           @PathVariable("storageId") String storageId,
                                           @ApiParam(value = "The repositoryId", required = true)
                                           @PathVariable("repositoryId") String repositoryId,
                                           @ApiParam(value = "The repository path", required = false)
                                           @PathVariable("path") String path,
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
                      
            RepositoryProvider respositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());
            Path dirPath = respositoryProvider.resolvePath(storageId, repositoryId, path);
                                    
            if (dirPath == null || !Files.exists(dirPath))
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body("{ 'error': 'The requested repository path was not found.' }");
            }
            
            DirectoryContent content = directoryContentFetcher.fetchDirectoryContent(dirPath);
            
            return ResponseEntity.ok(objectMapper.writer().writeValueAsString(content));
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
            throws IOException
    {
        logger.debug("Requested browsing for storages");

        try
        {   
            Set<Entry<String, Storage>> storages = configurationManager.getConfiguration().getStorages().entrySet();

            List<Path> storagePaths = new ArrayList<Path>();

            for(Entry<String, Storage> entry : storages)
            {
                Storage storage = entry.getValue();
                String storageBaseDir = storage.getBasedir();
                storagePaths.add(Paths.get(storageBaseDir));
            }
            DirectoryContent content = directoryContentFetcher.fetchDirectoryContent(storagePaths);

            generateHTML(request, response, content, "", "", "");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
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

        try
        {
            Storage storage = configurationManager.getConfiguration().getStorage(storageId);
            if (storage == null) 
            {
                response.sendError(HttpStatus.NOT_FOUND.value(), "Unable to find storage by ID " + storageId);
                return;
            }
            
            Set<Entry<String, Repository>> repositories = storage.getRepositories().entrySet();
            
            List<Path> repositoryPaths = new ArrayList<Path>();
            
            for(Entry<String, Repository> entry : repositories)
            {
                Repository repository = entry.getValue();
                RepositoryProvider respositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());
                Path repositoryBaseDir = respositoryProvider.resolvePath(storageId, repository.getId(), "");
                repositoryPaths.add(repositoryBaseDir);
            }
                        
            DirectoryContent content = directoryContentFetcher.fetchDirectoryContent(repositoryPaths);                        
            
            generateHTML(request, response, content, storageId, "", "");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }
    
    @ApiOperation(value = "List the contents for a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The list was returned."),
                            @ApiResponse(code = 404, message = "The requested storage, repository, or path was not found."),
                            @ApiResponse(code = 500, message = "An error occurred."),
                            @ApiResponse(code = 503, message = "Repository not in service")})
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = { "{storageId}/{repositoryId}/{path:.+}" },
                    method = RequestMethod.GET, 
                    produces = MediaType.TEXT_HTML_VALUE)
    public void browseRepositoryContents(@ApiParam(value = "The storageId", required = true)
                                         @PathVariable("storageId") String storageId,
                                         @ApiParam(value = "The repositoryId", required = true)
                                         @PathVariable("repositoryId") String repositoryId,
                                         @ApiParam(value = "The repository path", required = false)
                                         @PathVariable("path") String path,
                                         HttpServletRequest request,
                                         HttpServletResponse response)
                throws Exception
    {           
        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        if (storage == null)
        {
            logger.error("Unable to find storage by ID " + storageId);

            response.sendError(HttpStatus.NOT_FOUND.value(), "Unable to find storage by ID " + storageId);

            return;
        }

        Repository repository = storage.getRepository(repositoryId);
        if (repository == null)
        {
            logger.error("Unable to find repository by ID " + repositoryId + " for storage " + storageId);

            response.sendError(HttpStatus.NOT_FOUND.value(),
                               "Unable to find repository by ID " + repositoryId + " for storage " + storageId);
            return;
        }

        if (!repository.isInService())
        {
            logger.error("Repository is not in service...");

            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());

            return;
        }
        
        RepositoryProvider respositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());
        Path dirPath = respositoryProvider.resolvePath(storageId, repositoryId, path);
        
        if (dirPath == null || !Files.exists(dirPath))
        {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;                                 
        }
        
        logger.debug("Requested browsing for {}/{}/{} ", storageId, repositoryId, path);
        
        if (repository.allowsDirectoryBrowsing() && probeForDirectoryListing(repository, path))
        {
            try
            {
                DirectoryContent content = directoryContentFetcher.fetchDirectoryContent(dirPath);
                //URL url = artifactResolutionService.resolveResource(storageId, repositoryId, path);
                generateHTML(request, response, content, storageId, repositoryId, path);
            }
            catch (Exception e)
            {
                logger.debug("Unable to generate directory listing for " +
                        "/" + storageId + "/" + repositoryId + "/" + path, e);

                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            return;
        }
        return;
    }    
    
    protected void generateHTML(HttpServletRequest request,
                                HttpServletResponse response,
                                DirectoryContent content,
                                String storageId,
                                String repositoryId,
                                String path)
    {
        if(content == null)
        {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            logger.debug("Could not retrive /storages base directory");
            return;
        }
                
        String requestUri = request.getRequestURI();
        
        if (!requestUri.endsWith("/"))
        {
            try
            {
                response.sendRedirect(requestUri + "/");
            }
            catch (IOException e)
            {
                logger.debug("Error redirecting to " + requestUri + "/");
            }
            return;
        }

        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            sb.append("<head>");
            sb.append(
                    "<style>body{font-family: \"Trebuchet MS\", verdana, lucida, arial, helvetica, sans-serif;} table tr {text-align: left;}</style>");
            sb.append("<title>Index of " + request.getRequestURI() + "</title>");
            sb.append("</head>");
            sb.append("<body>");
            sb.append("<h1>Index of " + request.getRequestURI() + "</h1>");
            sb.append("<table cellspacing=\"10\">");
            sb.append("<tr>");
            sb.append("<th>Name</th>");
            sb.append("<th>Last modified</th>");
            sb.append("<th>Size</th>");
            sb.append("<th>Description</th>");
            sb.append("</tr>");
            sb.append("<tr>");
            sb.append("<td colspan=4><a href=\"..\">..</a></td>");
            sb.append("</tr>");
           
            for (FileContent fileContent : content.getDirectories())
            {
                appendFile(sb, fileContent, requestUri, true, storageId, repositoryId, path);
            }
                       
            for (FileContent fileContent : content.getFiles())
            {
                appendFile(sb, fileContent, requestUri, false, storageId, repositoryId, path);
            }

            sb.append("</table>");
            sb.append("</body>");
            sb.append("</html>");

            response.setContentType("text/html;charset=UTF-8");
            response.setStatus(HttpStatus.OK.value());
            response.getWriter()
                    .write(sb.toString());
            response.getWriter()
                    .flush();
            response.getWriter()
                    .close();

        }
        catch (IOException e)
        {
            logger.error(" error accessing requested directory");

            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    
    private boolean appendFile(StringBuilder sb,
                               FileContent fileContent,
                               String requestUri,
                               boolean isDirectory,
                               String storageId,
                               String repositoryId,
                               String path)
            throws IOException
    {
        String name = fileContent.getName();
        String size = fileContent.getSize();
        String lastModified = fileContent.getLastModified();

        sb.append("<tr>");

        if(isDirectory)
        {
            sb.append("<td><a href='" + URLEncoder.encode(name, "UTF-8") + 
                      "/'>" + name + "/" + "</a></td>");
        }
        else
        {   
            String artifactPath =  path + "/" + name;
            URL artifactUrl = artifactResolutionService.resolveResource(storageId, repositoryId, artifactPath);           
            sb.append("<td><a href='" + artifactUrl + "'>" + name + "</a></td>");
        }
        sb.append("<td>" + lastModified + "</td>");
        sb.append("<td>" + size + "</td>");
        sb.append("<td></td>");
        sb.append("</tr>");
        return true;
    }
}
