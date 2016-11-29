package org.carlspring.strongbox.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.apache.commons.fileupload.FileUploadBase.FileUploadIOException;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.security.exceptions.SecurityTokenException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import ru.aristar.jnuget.files.TempNupkgFile;

/**
 * This Controller used to handle Nuget requests.
 * 
 * @author Sergey Bespalov
 *
 */
@RestController
@RequestMapping(path = NugetPackageController.ROOT_CONTEXT, headers = "user-agent=NuGet/*")
public class NugetPackageController extends BaseArtifactController
{

    private static final Logger logger = LogManager.getLogger(NugetPackageController.class.getName());

    public final static String ROOT_CONTEXT = "/storages";

    @Autowired
    private UserService userService;
    

    /**
     * This method is used to check storage availability.<br>
     * For example NuGet pings the root without credentials to determine if the
     * repository is healthy. If this receives a 401 response then NuGet will
     * prompt for authentication.
     * 
     * @return
     */
    @ApiOperation(value = "Used to check storage availability")
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Storage available."),
                            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Storage requires authorization.") })
    @RequestMapping(path = { "{storageId}/{repositoryId}", "greet" }, method = RequestMethod.GET)
    public
           ResponseEntity greet()
    {
        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    @ApiOperation(value = "Used to deploy a package")
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "The package was deployed successfully."),
                            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    @RequestMapping(path = "{storageId}/{repositoryId}", method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA)
    public
           ResponseEntity putPackage(@RequestHeader(name = "X-NuGet-ApiKey", required = false) String apiKey,
                                     @ApiParam(value = "The storageId", required = true) @PathVariable(name = "storageId") String storageId,
                                     @ApiParam(value = "The repositoryId", required = true) @PathVariable(name = "repositoryId") String repositoryId,
                                     @RequestHeader("content-type") String contentType,
                                     HttpServletRequest request)
    {
        logger.info(String.format("Nuget push request: storageId-[%s]; repositoryId-[%s]",
                storageId,
                repositoryId));

        String userName = getUserName();
        if (!verify(userName, apiKey))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        URI resourceUri;
        try
        {
            ServletInputStream is = request.getInputStream();
            FileInputStream packagePartInputStream = extractPackageMultipartStream(extractBoundary(contentType), is);

            if (packagePartInputStream == null)
            {
                logger.error(
                        String.format("Failed to extract Nuget package from request: storageId-[%s]; repositoryId-[%s]",
                                storageId, repositoryId));
                return ResponseEntity.badRequest().build();
            }

            resourceUri = storePackage(storageId, repositoryId, packagePartInputStream);
        }
        catch (Exception e)
        {
            logger.error(String.format("Failed to process Nuget push request: storageId-[%s]; repositoryId-[%s]",
                    storageId, repositoryId), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.created(resourceUri).build();
    }

    @ApiOperation(value = "Used to download a package")
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "The package was downloaded successfully."),
                            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(path = "{storageId}/{repositoryId}/download/{packageId}/{packageVersion}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM)
    public
           ResponseEntity<?> getPackage(@ApiParam(value = "The storageId", required = true) @PathVariable(name = "storageId") String storageId,
                                        @ApiParam(value = "The repositoryId", required = true) @PathVariable(name = "repositoryId") String repositoryId,
                                        @ApiParam(value = "The packageId", required = true) @PathVariable(name = "packageId") String packageId,
                                        @ApiParam(value = "The packageVersion", required = true) @PathVariable(name = "packageVersion") String packageVersion)
    {
        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        if (!repository.isInService())
        {
            logger.error("Repository is not in service...");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        String path = String.format("%s/%s/%s.%s.nupkg", packageId, packageVersion,
                packageId, packageVersion);

        try
        {
            InputStream is = (ArtifactInputStream) getArtifactManagementService().resolve(storageId, repositoryId,
                    path);
            if (is == null)
            {
                return ResponseEntity.notFound().build();
            }

            try (TempNupkgFile nupkgFile = new TempNupkgFile(is))
            {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Length", String.valueOf(nupkgFile.getSize()));
                headers.add("Content-Disposition",
                        String.format("attachment; filename=\"%s\"", nupkgFile.getFileName()));
                return new ResponseEntity<Resource>(new InputStreamResource(nupkgFile.getStream()),
                        headers,
                        HttpStatus.OK);

            }

        }
        catch (Exception e)
        {
            logger.error(String.format(
                    "Failed to process Nuget get request: storageId-[%s]; repositoryId-[%s]; packageId-[%s]; version-[%s]",
                    storageId, repositoryId, packageId, packageVersion), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private
            String extractBoundary(String contentType)
    {
        String boundaryString = "";
        Pattern pattern = Pattern.compile("multipart/form-data; boundary=(.*)");
        Matcher matcher = pattern.matcher(contentType);
        if (matcher.matches())
        {
            boundaryString = matcher.group(1);
        }
        return boundaryString;
    }

    private
            FileInputStream extractPackageMultipartStream(String boundaryString,
                                                          ServletInputStream is) throws IOException, FileNotFoundException, FileUploadIOException, MalformedStreamException
    {

        if (StringUtils.isEmpty(boundaryString))
        {
            return null;
        }

        File packagePartFile = File.createTempFile("nupkg", "part");
        try (FileOutputStream packagePartOutputStream = new FileOutputStream(packagePartFile))
        {

            byte[] boundary = boundaryString.getBytes();
            MultipartStream multipartStream = new MultipartStream(is, boundary);

            boolean nextPart = multipartStream.skipPreamble();
            while (nextPart)
            {
                String header = multipartStream.readHeaders();

                // Package Multipart Header should be like follows:
                // Content-Disposition: form-data; name="package";
                // filename="package"
                // Content-Type: application/octet-stream
                if (!header.contains("\"package\""))
                {
                    continue;
                }
                try
                {
                    multipartStream.readBodyData(packagePartOutputStream);
                    nextPart = multipartStream.readBoundary();
                }
                catch (MultipartStream.MalformedStreamException e)
                {
                    // Seems that this is normal for Nuget push request
                }
                break;
            }
        }

        return new FileInputStream(packagePartFile);
    }

    private
            URI storePackage(String storageId,
                             String repositoryId,
                             InputStream is) throws Exception
    {

        try (TempNupkgFile nupkgFile = new TempNupkgFile(is))
        {
            String path = String.format("%s/%s/%s.%s.nupkg", nupkgFile.getId(), nupkgFile.getVersion(),
                    nupkgFile.getId(), nupkgFile.getVersion());
            artifactManagementService.store(storageId, repositoryId, path, nupkgFile.getStream());

            File nuspecFile = File.createTempFile(nupkgFile.getId(), "nuspec");
            try (FileOutputStream fileOutputStream = new FileOutputStream(nuspecFile))
            {
                nupkgFile.getNuspecFile().saveTo(fileOutputStream);
            }
            path = String.format("%s/%s/%s.nuspec", nupkgFile.getId(), nupkgFile.getVersion(), nupkgFile.getId());
            artifactManagementService.store(storageId, repositoryId, path, new FileInputStream(nuspecFile));

            File hashFile = File.createTempFile(String.format("%s.%s", nupkgFile.getId(),
                    nupkgFile.getVersion()), "nupkg.sha512");
            nupkgFile.getHash().saveTo(hashFile);

            path = String.format("%s/%s/%s.%s.nupkg.sha512", nupkgFile.getId(), nupkgFile.getVersion(),
                    nupkgFile.getId(), nupkgFile.getVersion());
            artifactManagementService.store(storageId, repositoryId, path, new FileInputStream(hashFile));
        }

        return new URI("");
    }

    private
            boolean verify(String userName,
                           String apiKey)
    {
        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(apiKey))
        {
            return false;
        }
        
        try
        {
            userService.verifySecurityToken(userName, apiKey);
        }
        catch (SecurityTokenException e)
        {
            return false;
        }
        
        return true;
    }

    private
            String getUserName()
    {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext == null ? null : securityContext.getAuthentication();
        String userName = authentication == null ? null : authentication.getName();
        return userName;
    }

}
