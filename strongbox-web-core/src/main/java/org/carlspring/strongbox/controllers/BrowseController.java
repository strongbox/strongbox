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
import java.util.Optional;
import java.util.regex.Matcher;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.carlspring.strongbox.domain.DirectoryContent;
import org.carlspring.strongbox.domain.FileContent;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.services.impl.ArtifactResolutionServiceImpl;
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

    @Inject
    private ArtifactResolutionServiceImpl artifactResolutionService;
    
    @ApiOperation(value = "List configured storages.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The list was returned."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> browse()
    {
        try
        {   
            Path dirPath = getStoragesDir();
            DirectoryContent content = new DirectoryContent(dirPath);
            
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
            
            Path dirPath = Paths.get(storage.getBasedir());
            DirectoryContent content = new DirectoryContent(dirPath);            
            
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
            Path baseDir = Paths.get(repository.getBasedir().toString(), subPath.replaceAll("/", Matcher.quoteReplacement(File.separator)));
            
            if (!Files.exists(baseDir))
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body("{ 'error': 'The requested repository path was not found.' }");
            }
            
            DirectoryContent content = new DirectoryContent(baseDir);
            
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
            Path dirPath = getStoragesDir();
            DirectoryContent content = new DirectoryContent(dirPath);
            
            generateHTML(request, response, content, null);
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
            
            Path dirPath = Paths.get(storage.getBasedir());
            DirectoryContent content = new DirectoryContent(dirPath);            
            
            generateHTML(request, response, content, null);
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
        
        String matchedPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String subPath = new AntPathMatcher().extractPathWithinPattern(matchedPattern, request.getRequestURI());
        Path baseDir = Paths.get(repository.getBasedir().toString(), subPath.replaceAll("/", Matcher.quoteReplacement(File.separator)));
                
        logger.debug("Requested browsing for {}/{}/{} ", storageId, repositoryId, subPath);
        
        if (repository.allowsDirectoryBrowsing() && probeForDirectoryListing(repository, subPath))
        {
            try
            {
                DirectoryContent content = new DirectoryContent(baseDir);
                URL url = artifactResolutionService.resolveResource(storageId, repositoryId, subPath);
                generateHTML(request, response, content, url);
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
                                URL url)
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
                appendFile(sb, fileContent, requestUri, true, url);
            }
                        
            if(url == null && !content.getFiles().isEmpty())
            {
                logger.debug("Files are present outside a repository");
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                return;
            }
            
            for (FileContent fileContent : content.getFiles())
            {
                appendFile(sb, fileContent, requestUri, false, url);
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
                               URL url)
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
            String storagesUrl = url.toString() + "/" + URLEncoder.encode(name, "UTF-8");
            sb.append("<td><a href='" + storagesUrl + "'>" + name + "</a></td>");
        }
        sb.append("<td>" + lastModified + "</td>");
        sb.append("<td>" + size + "</td>");
        sb.append("<td></td>");
        sb.append("</tr>");
        return true;
    }
    
    protected Path getStoragesDir()
    {
        Path dirPath = null;
        
        if (System.getProperty("strongbox.storage.booter.basedir") != null)
        {
            dirPath = Paths.get(System.getProperty("strongbox.storage.booter.basedir"));
        }
        else
        {
            // Assuming this invocation is related to tests:
            dirPath =  Paths.get(ConfigurationResourceResolver.getVaultDirectory() + "/storages/");
        }        
        
        return dirPath; 
    }
}
