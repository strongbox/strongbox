package org.carlspring.strongbox.controllers.layout.pypi;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.artifact.metadata.PypiArtifactMetadata;
import org.carlspring.strongbox.controllers.BaseArtifactController;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidationException;
import org.carlspring.strongbox.utils.ArtifactControllerHelper;
import org.carlspring.strongbox.web.LayoutRequestMapping;
import org.carlspring.strongbox.web.RepositoryMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.core.MediaType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Sets;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Rest End Points for Pypi Artifacts requests.
 * This controller will be Entry point for various pip commands.
 * 
 * @author ankit.tomar
 * 
 */
@RestController
@LayoutRequestMapping(PypiArtifactCoordinates.LAYOUT_NAME)
public class PypiArtifactController extends BaseArtifactController
{

    private static final Set<String> VALID_ACTIONS = Sets.newHashSet("file_upload");

    private static final Set<String> VALID_FILE_TYPES = Sets.newHashSet("sdist", "bdist_wheel");

    private static final String CONTENT_DISPOSITION = "content-disposition";

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    @ApiOperation(value = "This end point will be used to upload and delete a python package based on action passed in request")
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "python package was deployed successfully."),
                            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "An error occurred while executing request."),
                            @ApiResponse(code = HttpURLConnection.HTTP_UNAVAILABLE, message = "Service Unavailable.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    @RequestMapping(path = "{storageId}/{repositoryId}/", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA)
    public ResponseEntity<String> uploadOrRemovePackage(
                                                        @RepositoryMapping Repository repository,
                                                        HttpServletRequest request)
    {

        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        logger.info("pip upload/uninstall request for storageId -> [{}] , repositoryId -> [{}]", storageId,
                    repositoryId);

        final String contentType = request.getHeader("content-type");
        String formDataBoundary = ArtifactControllerHelper.extractBoundaryFromContentType(contentType);

        if (StringUtils.isEmpty(formDataBoundary))
        {
            return ResponseEntity.status(HttpURLConnection.HTTP_BAD_REQUEST)
                                 .body("multipart/form-data separator boundary is mising in header.");
        }

        try
        {
            if (CollectionUtils.isEmpty(request.getParts()))
            {
                return ResponseEntity.status(HttpURLConnection.HTTP_BAD_REQUEST)
                                     .body("multipart/form-data parts missing.");
            }

            PypiArtifactMetadata pypiArtifactMetadata = getPypiArtifactMetadata(request);

            if (!isValidAction(pypiArtifactMetadata.getAction()))
            {
                return ResponseEntity.status(HttpURLConnection.HTTP_BAD_REQUEST)
                                     .body("Invalid value for \":action\" parameter. Valid action values are "
                                             + VALID_ACTIONS);
            }

            return validateAndUploadPackage(pypiArtifactMetadata, formDataBoundary, repository, request);
        }
        catch (Exception e)
        {
            logger.error("Failed to process pypi upload/uninstall request for storageId -> [{}] , repositoryId -> [{}]",
                         storageId, repositoryId, e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    private ResponseEntity<String> validateAndUploadPackage(PypiArtifactMetadata pypiArtifactMetadata,
                                                            String formDataBoundary,
                                                            Repository repository,
                                                            HttpServletRequest request)
        throws IOException,
        ServletException,
        ProviderImplementationException,
        ArtifactCoordinatesValidationException
    {
        if (!isValidFileType(pypiArtifactMetadata.getFileType()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Invalid value for \"filetype\" parameter.Valid values are " + VALID_FILE_TYPES);
        }

        String fileName = getFileNameFromMultiPartStream(request);
        if (StringUtils.isEmpty(fileName))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Null or Empty File Name.");
        }

        if (!isValidFileName(pypiArtifactMetadata.getName(), pypiArtifactMetadata.getVersion(),
                             pypiArtifactMetadata.getFileType(), fileName))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File name does not match metadata");
        }

        Part filePart = getFilePartFromMultiPartStream(request);
        if (filePart == null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is missing in Multipart Stream");
        }

        // Appending timestamp in temp file name to avoid file content
        // overriding in case multiple of simulataneous uploads.
        long currentTime = System.currentTimeMillis();
        InputStream inputStream = ArtifactControllerHelper.extractPackageFromMultipartStream("pypi" + currentTime,
                                                                                             ".tar.gz",
                                                                                             formDataBoundary,
                                                                                             "content", filePart);

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository.getStorage().getId(),
                                                                       repository.getId(),
                                                                       String.format("%s/%s/%s",
                                                                                     pypiArtifactMetadata.getName(),
                                                                                     pypiArtifactMetadata.getVersion(),
                                                                                     fileName));
        artifactManagementService.validateAndStore(repositoryPath, inputStream);

        return ResponseEntity.status(HttpStatus.OK).body("The artifact was deployed successfully.");
    }

    private Part getFilePartFromMultiPartStream(HttpServletRequest request)
        throws IOException,
        ServletException
    {
        for (Part part : request.getParts())
        {
            String partName = getPartName(part);
            String[] partNameTokens = partName.split(";");
            if (partNameTokens.length == 2)
            {
                return part;
            }
        }
        return null;
    }

    private String getFileNameFromMultiPartStream(HttpServletRequest request)
        throws IOException,
        ServletException
    {

        for (Part part : request.getParts())
        {
            String partName = getPartName(part);
            String[] partNameTokens = partName.split(";");
            if (partNameTokens.length == 2)
            {
                String fileNameKeyValue = partNameTokens[1];
                return fileNameKeyValue.substring(fileNameKeyValue.indexOf('=') + 1);
            }
        }
        return null;
    }

    private boolean isValidFileType(String fileType)
    {
        if (StringUtils.isEmpty(fileType))
        {
            return false;
        }
        return VALID_FILE_TYPES.contains(fileType);
    }

    private PypiArtifactMetadata getPypiArtifactMetadata(HttpServletRequest request)
        throws UnsupportedEncodingException,
        IOException,
        ServletException
    {
        PypiArtifactMetadata pypiArtifactMetadata = new PypiArtifactMetadata();
        for (Part part : request.getParts())
        {
            String partName = getPartName(part);
            switch (partName)
            {
                case "comment":
                {
                    pypiArtifactMetadata.setComment(getTextValueFromMultiPartStream(part, request.getCharacterEncoding()));
                    break;
                }
                case "metadata_version":
                {
                    pypiArtifactMetadata.setMetdataVersion(getTextValueFromMultiPartStream(part,
                                                                                           request.getCharacterEncoding()));
                    break;
                }
                case "filetype":
                {
                    pypiArtifactMetadata.setFileType(getTextValueFromMultiPartStream(part, request.getCharacterEncoding()));
                    break;
                }
                case "protcol_version":
                {
                    pypiArtifactMetadata.setProtcolVersion(getTextValueFromMultiPartStream(part,
                                                                                           request.getCharacterEncoding()));
                    break;
                }
                case "author":
                {
                    pypiArtifactMetadata.setAuthor(getTextValueFromMultiPartStream(part, request.getCharacterEncoding()));
                    break;
                }
                case "home_page":
                {
                    pypiArtifactMetadata.setHomePage(getTextValueFromMultiPartStream(part, request.getCharacterEncoding()));
                    break;
                }
                case "download_url":
                {
                    pypiArtifactMetadata.setDownloadUrl(getTextValueFromMultiPartStream(part,
                                                                                        request.getCharacterEncoding()));
                    break;
                }
                case "description":
                {
                    pypiArtifactMetadata.setDescription(getTextValueFromMultiPartStream(part,
                                                                                        request.getCharacterEncoding()));
                    break;
                }
                case "version":
                {
                    pypiArtifactMetadata.setVersion(getTextValueFromMultiPartStream(part, request.getCharacterEncoding()));
                    break;
                }
                case "platform":
                {
                    pypiArtifactMetadata.setPlatform(getTextValueFromMultiPartStream(part, request.getCharacterEncoding()));
                    break;
                }
                case "md5_digest":
                {
                    pypiArtifactMetadata.setMd5Digest(getTextValueFromMultiPartStream(part,
                                                                                      request.getCharacterEncoding()));
                    break;
                }
                case ":action":
                {
                    pypiArtifactMetadata.setAction(getTextValueFromMultiPartStream(part, request.getCharacterEncoding()));
                    break;
                }
                case "name":
                {
                    pypiArtifactMetadata.setName(getTextValueFromMultiPartStream(part, request.getCharacterEncoding()));
                    break;
                }
                case "license":
                {
                    pypiArtifactMetadata.setLicense(getTextValueFromMultiPartStream(part, request.getCharacterEncoding()));
                    break;
                }
                case "pyversion":
                {
                    pypiArtifactMetadata.setPyVersion(getTextValueFromMultiPartStream(part,
                                                                                      request.getCharacterEncoding()));
                    break;
                }
                case "summary":
                {
                    pypiArtifactMetadata.setSummary(getTextValueFromMultiPartStream(part, request.getCharacterEncoding()));
                    break;
                }
                case "author_email":
                {
                    pypiArtifactMetadata.setAuthorEmail(getTextValueFromMultiPartStream(part,
                                                                                        request.getCharacterEncoding()));
                    break;
                }
            }
        }
        return pypiArtifactMetadata;
    }

    private String getTextValueFromMultiPartStream(Part part,
                                                   String encoding)
        throws UnsupportedEncodingException,
        IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream(), encoding));
        StringBuilder value = new StringBuilder();
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        for (int length = 0; (length = reader.read(buffer)) > 0;)
        {
            value.append(buffer, 0, length);
        }
        return value.toString();
    }

    private String getPartName(Part part)
    {
        String header = part.getHeader(CONTENT_DISPOSITION);
        return header.substring(header.indexOf('=') + 1).replace("\"", "");
    }

    private boolean isValidAction(String action)
    {
        if (StringUtils.isEmpty(action))
        {
            return false;
        }
        return VALID_ACTIONS.contains(action);
    }

    public boolean isValidFileName(String name,
                                   String version,
                                   String fileType,
                                   String filename)
    {
        StringBuilder builder = new StringBuilder(name);

        builder.append("-").append(version).append(fileType.equals("bdist_wheel") ? ".whl" : ".tar.gz");

        return filename.equals(builder.toString());
    }

}
