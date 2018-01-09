package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;

import io.swagger.annotations.Api;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.DirectoryFileComparator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Api(value = "/storages")
public abstract class BaseArtifactController
        extends BaseController
{

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private StorageProviderRegistry storageProviderRegistry;


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
        if (!file.isHidden() && !path.startsWith(".") && !path.contains("/."))
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
            sb.append("</tr>");
            sb.append("<tr>");
            sb.append("<td colspan=3><a href='..'>..</a></td>");
            sb.append("</tr>");

            File[] childFiles = file.listFiles();
            if (childFiles != null)
            {
                Arrays.sort(childFiles, DirectoryFileComparator.DIRECTORY_COMPARATOR);

                for (File childFile : childFiles)
                {
                    String name = childFile.getName();

                    if (name.startsWith(".") || childFile.isHidden())
                    {
                        continue;
                    }

                    String lastModified = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(
                            new Date(childFile.lastModified()));

                    sb.append("<tr>");
                    sb.append("<td><a href='" + URLEncoder.encode(name, "UTF-8") + (childFile.isDirectory() ?
                                                                                    "/" : "") + "'>" + name +
                              (childFile.isDirectory() ? "/" : "") + "</a></td>");
                    sb.append("<td>" + lastModified + "</td>");
                    sb.append("<td>" + FileUtils.byteCountToDisplaySize(childFile.length()) + "</td>");
                    sb.append("</tr>");
                }
            }

            sb.append("</table>");
            sb.append("</body>");
            sb.append("</html>");

            response.setContentType("text/html;charset=UTF-8");
            response.setStatus(HttpStatus.FOUND.value());
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

    protected void setMediaTypeHeader(Repository repository,
                                      String path,
                                      HttpServletResponse response)
            throws IOException
    {
        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        RepositoryPath artifactPath = layoutProvider.resolve(repository).resolve(path);
        RepositoryFileAttributes artifactFileAttributes = Files.readAttributes(artifactPath,
                                                                               RepositoryFileAttributes.class);

        // TODO: This is far from optimal and will need to have a content type approach at some point:
        if (artifactFileAttributes.isChecksum())
        {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        }
        else if (artifactFileAttributes.isMetadata())
        {
            response.setContentType(MediaType.APPLICATION_XML_VALUE);
        }
        else
        {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        }
    }

}
