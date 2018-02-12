package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.utils.ArtifactControllerHelper;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;

import io.swagger.annotations.Api;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(value = "/storages")
public abstract class BaseArtifactController
        extends BaseController
{

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private StorageProviderRegistry storageProviderRegistry;

    @Inject
    protected ArtifactManagementService artifactManagementService;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;
    
    // ----------------------------------------------------------------------------------------------------------------
    // Common-purpose methods

    public Storage getStorage(String storageId)
    {
        return configurationManager.getConfiguration()
                                   .getStorage(storageId);
    }

    public Repository getRepository(String storageId,
                                    String repositoryId)
    {
        return getStorage(storageId).getRepository(repositoryId);
    }

    public LayoutProviderRegistry getLayoutProviderRegistry()
    {
        return layoutProviderRegistry;
    }

    public void setLayoutProviderRegistry(LayoutProviderRegistry layoutProviderRegistry)
    {
        this.layoutProviderRegistry = layoutProviderRegistry;
    }

    public StorageProviderRegistry getStorageProviderRegistry()
    {
        return storageProviderRegistry;
    }

    public void setStorageProviderRegistry(StorageProviderRegistry storageProviderRegistry)
    {
        this.storageProviderRegistry = storageProviderRegistry;
    }
    
    protected boolean probeForDirectoryListing(Repository repository,
                                               String path)
    {
        String filePath = path.replaceAll("/", Matcher.quoteReplacement(File.separator));

        String dir = repository.getBasedir() + File.separator + filePath;

        File file = new File(dir);

        // Do not allow .index and .trash directories (or any other directory starting with ".") to be browseable.
        // NB: Files will still be downloadable.
        if (isPermittedForDirectoryListing(file, path))
        {
            if (file.exists() && file.isDirectory())
            {
                return true;
            }

            file = new File(dir + File.separator);

            return file.exists() && file.isDirectory();
        }
        else
        {
            return false;
        }
    }
    
    protected boolean isPermittedForDirectoryListing(File file,
                                                     String path)
    {
        return (!file.isHidden() && !path.startsWith(".") && !path.contains("/."));        
    }

    protected void getDirectoryListing(HttpServletRequest request,
                                       HttpServletResponse response)
    {
        String dirPath = null;
        
        if (System.getProperty("strongbox.storage.booter.basedir") != null)
        {
            dirPath = System.getProperty("strongbox.storage.booter.basedir");
        }
        else
        {
            // Assuming this invocation is related to tests:
            dirPath =  ConfigurationResourceResolver.getVaultDirectory() + "/storages/";
        }        
        generateDirectoryListing(dirPath, request, response);
    }
    
    protected void getDirectoryListing(Storage storage,
                                       HttpServletRequest request,
                                       HttpServletResponse response)
    {
        String dirPath = storage.getBasedir();
        
        generateDirectoryListing(dirPath, request, response);
    }
    
    protected void getDirectoryListing(Repository repository,
                                       String path,
                                       HttpServletRequest request,
                                       HttpServletResponse response)
    {
        path = path.replaceAll("/", Matcher.quoteReplacement(File.separator));

        if (request == null)
        {
            throw new RuntimeException("Unable to retrieve HTTP request from execution context");
        }

        String dirPath = repository.getBasedir() + File.separator + path;
                
        generateDirectoryListing(dirPath, request, response);
    }

    protected void generateDirectoryListing(String dirPath,
                                            HttpServletRequest request,
                                            HttpServletResponse response)
    {   
        if(dirPath == null)
        {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            logger.debug("Could not retrive /storages base directory");
            return;
        }
        
        File file = new File(dirPath);
        String requestUri = request.getRequestURI();
        
        if (file.isDirectory() && !requestUri.endsWith("/"))
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
            logger.debug(" browsing: " + file.toString());

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
            if (!request.getRequestURI().equalsIgnoreCase(BrowseStoragesController.ROOT_CONTEXT + "/"))
            {
                sb.append("<tr>");
                sb.append("<td colspan=4><a href=\"../\">..</a></td>");
                sb.append("</tr>");
            }

            String requestURL = request.getRequestURL().toString();
            File[] childFiles = file.listFiles();
            if (childFiles != null)
            {
                for (File dirFile : ArtifactControllerHelper.getDirectories(file))
                {
                    appendFile(sb, dirFile, requestURL);
                }

                for (File childFile : ArtifactControllerHelper.getFiles(file))
                {
                    appendFile(sb, childFile, requestURL);
                }
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
        catch (Exception e)
        {
            logger.error(" error accessing requested directory: " + file.getAbsolutePath(), e);

            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private boolean appendFile(StringBuilder sb,
                               File childFile,
                               final String requestURL)
            throws UnsupportedEncodingException
    {
        String name = childFile.getName();

        if (name.startsWith(".") || childFile.isHidden())
        {
            return false;
        }

        String lastModified = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(
                new Date(childFile.lastModified()));

        sb.append("<tr>");
        sb.append("<td><a href=\"" + requestURL + URLEncoder.encode(name, "UTF-8") +
                  (childFile.isDirectory() ? "/" : "") + "\">" + name + (childFile.isDirectory() ? "/" : "") +
                  "</a></td>");
        sb.append("<td>" + lastModified + "</td>");
        sb.append("<td>" + FileUtils.byteCountToDisplaySize(childFile.length()) + "</td>");
        sb.append("<td></td>");
        sb.append("</tr>");
        return true;
    }


    protected boolean provideArtifactDownloadResponse(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   HttpHeaders httpHeaders,
                                                   Repository repository,
                                                   String path)
        throws IOException,
        ArtifactTransportException,
        ProviderImplementationException,
        Exception
    {
        String storageId = repository.getStorage().getId();
        String repositoryId = repository.getId();
        
        RepositoryPath resolvedPath = artifactManagementService.getPath(storageId, repositoryId, path);
        logger.debug("Resolved path : " + resolvedPath);
        
        ArtifactControllerHelper.provideArtifactHeaders(response, resolvedPath);
        if (response.getStatus() == HttpStatus.NOT_FOUND.value())
        {
            return false;
        }
        else if (request.getMethod().equals(RequestMethod.HEAD.name()))
        {
            return true;
        }

        logger.debug("Proceeding downloading : " + resolvedPath);
        InputStream is = artifactManagementService.resolve(storageId, repositoryId, path);
        if (ArtifactControllerHelper.isRangedRequest(httpHeaders))
        {
            logger.debug("Detecting range request....");
            ArtifactControllerHelper.handlePartialDownload(is, httpHeaders, response);
        }

        artifactEventListenerRegistry.dispatchArtifactDownloadingEvent(storageId, repositoryId, path);
        copyToResponse(is, response);
        artifactEventListenerRegistry.dispatchArtifactDownloadedEvent(storageId, repositoryId, path);

        logger.debug("Download succeeded.");
        
        return true;
    }

}
