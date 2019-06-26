package org.carlspring.strongbox.controllers.layout.pypi;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.controllers.BaseArtifactController;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.web.LayoutRequestMapping;

import java.io.IOException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.carlspring.strongbox.web.RepositoryMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * This controller is used to handle PyPi requests.
 *
 * @author carlspring
 */
@RestController
@LayoutRequestMapping(PypiArtifactCoordinates.LAYOUT_NAME)
public class PypiArtifactController
        extends BaseArtifactController
{
    private static final Logger logger = LoggerFactory.getLogger(PypiArtifactController.class);

    @ApiOperation(value = "Used to deploy an pypi artifact")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The artifact was deployed successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('ARTIFACTS_DEPLOY')")
    @PostMapping(value = "{storageId}/{repositoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity upload(@RepositoryMapping Repository repository,
                                 @ApiParam(value = "The content", required = true)
                                 @RequestPart("content") MultipartFile multipartFile,
                                 @ApiParam(value = "The name of the PyPi artifact", required = true)
                                 @RequestParam("name") String name,
                                 @ApiParam(value = "The platform", required = true)
                                 @RequestParam(name = "platform", required = false) String platform,
                                 @ApiParam(value = "The version of the artifact", required = true)
                                 @RequestParam("version") String version,
                                 @ApiParam(value = "A summary for the artifact", required = true)
                                 @RequestParam("summary") String summary,
                                 @ApiParam(value = "A description of the artifact", required = true)
                                 @RequestParam("description") String description,
                                 @ApiParam(value = "The MD5 digest of the artifact", required = true)
                                 @RequestParam("md5_digest") String md5Digest,
                                 @ApiParam(value = "The artifact's license", required = true)
                                 @RequestParam("license") String license,
                                 @ApiParam(value = "The version of Python required for this artifact", required = true)
                                 @RequestParam("pyversion") String pythonVersion,
                                 @ApiParam(value = "The artifact's author", required = true)
                                 @RequestParam("author") String author,
                                 @ApiParam(value = "The artifact author's e-mail", required = true)
                                 @RequestParam("author_email") String authorEmail,
                                 @ApiParam(value = "The PyPi protocol version. (Please, note that there is" +
                                                   " typo in the PyPi implementation).")
                                 @RequestParam(value = "protcol_version", required = false) String protcolVersion,
                                 @ApiParam(value = "The PyPi protocol version.")
                                 @RequestParam(value = "protocol_version", required = false) String protocolVersion,
                                 @ApiParam(value = "The file type", required = true)
                                 @RequestParam("filetype") String fileType,
                                 @ApiParam(value = "The version of the metadata", required = true)
                                 @RequestParam("metadata_version") String metadataVersion,
                                 @ApiParam(value = "The project's home page", required = true)
                                 @RequestParam("home_page") String homePage,
                                 @ApiParam(value = "The artifact's download URL", required = true)
                                 @RequestParam("download_url") String downloadUrl)
            throws IOException
    {
        return provideArtifactUploading(repository, multipartFile.getOriginalFilename(), multipartFile.getInputStream());
    }

    @ApiOperation(value = "Used to retrieve pypi artifact")
    @ApiResponses(value = {@ApiResponse(code = 200, message = ""),
            @ApiResponse(code = 404, message = "Requested path not found."),
            @ApiResponse(code = 500, message = "Server error."),
            @ApiResponse(code = 503, message = "Repository currently not in service.")})
    @PreAuthorize("hasAuthority('ARTIFACTS_RESOLVE')")
    @RequestMapping(value = {"/{storageId}/{repositoryId}/{path:.+}"}, method = {RequestMethod.GET, RequestMethod.HEAD})
    public void download(@RepositoryMapping Repository repository,
                         @PathVariable String path,
                         @RequestHeader HttpHeaders headers,
                         HttpServletRequest request,
                         HttpServletResponse response)
            throws Exception
    {
        logger.debug("Downloading package {}", path);

        RepositoryPath repositoryPath = artifactResolutionService.resolvePath(repository.getId(), repository.getStorage().getId(), path);

        provideArtifactDownloadResponse(request, response, headers, repositoryPath);
    }

}
